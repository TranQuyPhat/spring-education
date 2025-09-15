// ClassService.java
package com.example.springboot_education.services.classes;

import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.classDTOs.*;
import com.example.springboot_education.entities.*;
import com.example.springboot_education.entities.ClassEntity.JoinMode;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.SubjectRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.classes.ClassUserRepository;
import com.example.springboot_education.services.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springboot_education.dtos.classDTOs.AddStudentToClassDTO;
import com.example.springboot_education.dtos.classDTOs.ClassResponseDTO;
import com.example.springboot_education.dtos.classDTOs.CreateClassDTO;
import com.example.springboot_education.dtos.classDTOs.PaginatedClassResponseDto;
import com.example.springboot_education.dtos.classDTOs.SubjectDTO;
import com.example.springboot_education.dtos.classDTOs.TeacherDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.entities.ClassUser;
import com.example.springboot_education.entities.ClassUserId;
import com.example.springboot_education.entities.Subject;
import com.example.springboot_education.entities.Users;

import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UsersJpaRepository userRepository;
    private final ClassUserRepository classUserRepository;
    private final SubjectRepository subjectRepository;
    @Autowired
    private SlackService slackService;
    public List<ClassResponseDTO> getAllClasses() {
        return classRepository.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClassResponseDTO getClassById(Integer id) {
        ClassEntity clazz = classRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class with id " + id));
        return toDTO(clazz);
    }

    public ClassEntity getClassEntityById(Integer id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class with id " + id));
    }

    @Transactional
    @LoggableAction(value = "CREATE", entity = "classes", description = "Tạo lớp học mới")
    // @CacheEvict(value = "classesOfTeacher", key = "#dto.teacherId")
    public ClassResponseDTO createClass(CreateClassDTO dto) {
        Users teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new EntityNotFoundException("Teacher"));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("Subject"));

        String yearPart = String.valueOf(dto.getSchoolYear());
        String semesterPart = "01";
        if ("Học kỳ 2".equalsIgnoreCase(dto.getSemester())) {
            semesterPart = "02";
        }
        String prefix = yearPart + semesterPart;

        // Lấy maxId hiện tại (lock row để tránh race condition)
        Integer maxId = classRepository.findMaxIdByPrefixForUpdate(Integer.parseInt(prefix + "000"));
        int nextNumber = (maxId != null) ? (maxId % 1000) + 1 : 1;

        Integer newId;
        // Lặp cho đến khi tìm được ID chưa tồn tại
        do {
            newId = Integer.parseInt(prefix + String.format("%03d", nextNumber));
            nextNumber++;
        } while (classRepository.existsById(newId));

        ClassEntity clazz = new ClassEntity();
        clazz.setId(newId);
        clazz.setClassName(dto.getClassName());
        clazz.setSchoolYear(dto.getSchoolYear());
        clazz.setSemester(dto.getSemester());
        clazz.setDescription(dto.getDescription());
        clazz.setTeacher(teacher);
        clazz.setSubject(subject);
        clazz.setJoinMode(dto.getJoinMode() != null ? dto.getJoinMode() : JoinMode.AUTO);
        clazz.setCreatedAt(Instant.now());

        ClassEntity saved = classRepository.save(clazz);


        String channelName = generateChannelName(saved);
        String channelDescription = String.format("Lớp %s - %s (%d - %s)",
                saved.getClassName(),
                saved.getSubject().getSubjectName(),
                saved.getSchoolYear(),
                saved.getSemester());

        log.info("Creating Slack PRIVATE channel for class {}: {}", saved.getId(), channelName);

        // 🔹 Tạo private channel
        SlackService.SlackChannelResult channelResult =
                slackService.createPrivateChannel(channelName, channelDescription);

        if (channelResult.isSuccess()) {
            saved.setSlackChannelId(channelResult.getChannelId());
            saved.setSlackInviteLink(channelResult.getInviteLink());
            classRepository.save(saved);

            // 🔹 Add teacher (nếu đã trong workspace)
            String teacherSlackId = slackService.lookupUserIdByEmail(teacher.getEmail());
            if (teacherSlackId != null) {
                slackService.inviteUserToChannel(channelResult.getChannelId(), teacherSlackId);
            }

            log.info("Created Slack channel {} for class {}", channelResult.getChannelName(), saved.getId());
        } else {
            log.error("Failed to create Slack channel for class {}: {}", saved.getId(), channelResult.getError());
        }

        return toDTO(saved);
    }

    @LoggableAction(value = "UPDATE", entity = "classes", description = "Update class")
    public ClassResponseDTO updateClass(Integer id, CreateClassDTO dto) {
        ClassEntity clazz = classRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class with id " + id));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("Subject"));
        Users teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new EntityNotFoundException("Teacher"));

        clazz.setClassName(dto.getClassName());
        clazz.setSchoolYear(dto.getSchoolYear());
        clazz.setSemester(dto.getSemester());
        clazz.setDescription(dto.getDescription());
        clazz.setSubject(subject);
        clazz.setTeacher(teacher);
        clazz.setJoinMode(dto.getJoinMode() != null ? dto.getJoinMode() : clazz.getJoinMode());
        clazz.setUpdatedAt(Instant.now());

        ClassEntity updated = classRepository.save(clazz);

        return toDTO(updated);
    }

    @LoggableAction(value = "DELETE", entity = "classes", description = "Delete class")
    public void deleteClass(Integer id) {
        ClassEntity clazz = classRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class"));

        classRepository.deleteById(id);
    }

    @LoggableAction(value = "CREATE", entity = "class_users", description = "Add student to class")
    public void addStudentToClass(AddStudentToClassDTO dto) {
        // Check if the student already exists in the class
        if (classUserRepository.existsByClassField_IdAndStudent_Id(dto.getClassId(), dto.getStudentId())) {
            throw new HttpException("Student already exists in this class!", HttpStatus.CONFLICT);
        }

        ClassEntity clazz = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class"));

        Users student = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Student"));

        ClassUser member = new ClassUser();
        ClassUserId id = new ClassUserId();
        id.setClassId(dto.getClassId());
        id.setStudentId(dto.getStudentId());

        member.setId(id);
        member.setClassField(clazz);
        member.setStudent(student);
        member.setJoinedAt(Instant.now());

        classUserRepository.save(member);
    }

    private ClassResponseDTO toDTO(ClassEntity clazz) {
        ClassResponseDTO dto = new ClassResponseDTO();
        dto.setId(clazz.getId());
        dto.setClassName(clazz.getClassName());
        dto.setSchoolYear(clazz.getSchoolYear());
        dto.setSemester(clazz.getSemester());
        dto.setDescription(clazz.getDescription());
        dto.setJoinMode(clazz.getJoinMode());
        dto.setCreatedAt(clazz.getCreatedAt());
        dto.setUpdatedAt(clazz.getUpdatedAt());

        if (clazz.getTeacher() != null) {
            TeacherDTO teacherDTO = new TeacherDTO();
            teacherDTO.setId(clazz.getTeacher().getId());
            teacherDTO.setFullName(clazz.getTeacher().getFullName());
            dto.setTeacher(teacherDTO);
        }

        if (clazz.getSubject() != null) {
            SubjectDTO subjectDTO = new SubjectDTO();
            subjectDTO.setId(clazz.getSubject().getId());
            subjectDTO.setName(clazz.getSubject().getSubjectName());
            dto.setSubject(subjectDTO);
        }
        dto.setSlackInviteLink(clazz.getSlackInviteLink());
        return dto;
    }

    // @Cacheable(value = "classesOfTeacher", key = "#teacherId")
    public List<ClassResponseDTO> getAllClassesOfTeacher(Integer teacherId) {
        List<ClassEntity> classes = classRepository.findByTeacher_Id(teacherId);

        return classes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PaginatedClassResponseDto getClassesOfTeacher(Integer teacherId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ClassEntity> pageResult = classRepository.findByTeacher_Id(teacherId, pageable);

        List<ClassResponseDTO> classDtos = pageResult.getContent()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PaginatedClassResponseDto response = new PaginatedClassResponseDto();
        response.setData(classDtos);
        response.setPageNumber(pageResult.getNumber());
        response.setPageSize(pageResult.getSize());
        response.setTotalRecords(pageResult.getTotalElements());
        response.setTotalPages(pageResult.getTotalPages());
        response.setHasNext(pageResult.hasNext());
        response.setHasPrevious(pageResult.hasPrevious());

        return response;
    }

    // websocket
    public Integer getTeacherIdOfClass(Integer classId) {
        return classRepository.findById(classId).map(c -> c.getTeacher().getId()) // lấy id từ quan hệ
                .orElseThrow(() -> new EntityNotFoundException("Class with id " + classId));
    }

    public String getClassName(Integer classId) {
        return classRepository.findById(classId)
                .map(ClassEntity::getClassName)
                .orElseThrow(() -> new EntityNotFoundException("Class with id " + classId));
    }


    private String generateChannelName(ClassEntity clazz) {
        // 1. Ghép tên lớp + môn học
        String raw = String.format("%s-%s",
                clazz.getClassName(),
                clazz.getSubject().getSubjectName()
        );

        // 2. Chuyển thành không dấu (remove accents)
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{M}", "");

        // 3. Chuyển thành lowercase, thay khoảng trắng & ký tự đặc biệt bằng "-"
        String name = withoutAccents.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")   // bỏ dấu --- liên tiếp
                .replaceAll("^-|-$", ""); // bỏ dấu - ở đầu/cuối

        // 4. Giới hạn 21 ký tự
        if (name.length() > 21) {
            name = name.substring(0, 21);
        }

        return name;
    }


    public List<ClassResponseDTO> searchClasses(String keyword) {
        return classRepository.findByClassNameContainingIgnoreCaseOrderByCreatedAtDesc(keyword)
                    .stream().map(this::toDTO)
                    .collect(Collectors.toList());
        }

    public List<ClassResponseDTO> getLatestClasses() {
        return classRepository.findTop10ByOrderByCreatedAtDesc()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

}
