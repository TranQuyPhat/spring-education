// ClassService.java
package com.example.springboot_education.services.classes;

import java.time.Instant; // Import annotation
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.classDTOs.AddStudentToClassDTO;
import com.example.springboot_education.dtos.classDTOs.ClassMemberDTO;
import com.example.springboot_education.dtos.classDTOs.ClassResponseDTO;
import com.example.springboot_education.dtos.classDTOs.CreateClassDTO;
import com.example.springboot_education.dtos.classDTOs.PaginatedClassResponseDto;
import com.example.springboot_education.dtos.classDTOs.SubjectDTO;
import com.example.springboot_education.dtos.classDTOs.TeacherDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.ClassUser;
import com.example.springboot_education.entities.ClassUserId;
import com.example.springboot_education.entities.Subject;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.ClassUserRepository;
import com.example.springboot_education.repositories.SubjectRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;


import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UsersJpaRepository userRepository;
    private final ClassUserRepository classUserRepository;
    private final SubjectRepository subjectRepository;
    // Xóa ActivityLogService khỏi constructor
    // private final ActivityLogService activityLogService;

    public List<ClassResponseDTO> getAllClasses() {
        return classRepository.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClassResponseDTO getClassById(Integer id) {
        ClassEntity clazz = classRepository.findById(id).orElseThrow();
        return toDTO(clazz);
    }

    @Transactional
    @LoggableAction(value = "CREATE", entity = "classes", description = "Tạo lớp học mới")
    public ClassResponseDTO createClass(CreateClassDTO dto) {
        Users teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow();
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học"));

        String yearPart = String.valueOf(dto.getSchoolYear());
        String semesterPart = "01";
        if ("Học kỳ 2".equalsIgnoreCase(dto.getSemester())) {
            semesterPart = "02";
        }
        String prefix = yearPart + semesterPart;

        Integer maxId = classRepository.findMaxIdByPrefixForUpdate(Integer.parseInt(prefix + "000"));
        int nextNumber = (maxId != null) ? (maxId % 1000) + 1 : 1;

        Integer newId = Integer.parseInt(prefix + String.format("%03d", nextNumber));

        ClassEntity clazz = new ClassEntity();
        clazz.setId(newId);
        clazz.setClassName(dto.getClassName());
        clazz.setSchoolYear(dto.getSchoolYear());
        clazz.setSemester(dto.getSemester());
        clazz.setDescription(dto.getDescription());
        clazz.setTeacher(teacher);
        clazz.setSubject(subject);
        clazz.setCreatedAt(Instant.now());

        ClassEntity saved = classRepository.save(clazz);

        // Xóa code ghi logs thủ công
        // activityLogService.log(...);

        return toDTO(saved);
    }

    @LoggableAction(value = "UPDATE", entity = "classes", description = "Cập nhật lớp học")
    public ClassResponseDTO updateClass(Integer id, CreateClassDTO dto) {
        ClassEntity clazz = classRepository.findById(id).orElseThrow();
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học"));

        clazz.setClassName(dto.getClassName());
        clazz.setSchoolYear(dto.getSchoolYear());
        clazz.setSemester(dto.getSemester());
        clazz.setDescription(dto.getDescription());
        clazz.setSubject(subject);
        clazz.setUpdatedAt(Instant.now());

        ClassEntity updated = classRepository.save(clazz);

        // Xóa code ghi logs thủ công
        // activityLogService.log(...);

        return toDTO(updated);
    }

    @LoggableAction(value = "DELETE", entity = "classes", description = "Xóa lớp học")
    public void deleteClass(Integer id) {
        ClassEntity clazz = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại"));

        // Xóa code ghi logs thủ công
        // activityLogService.log(...);

        classRepository.deleteById(id);
    }
    
    @LoggableAction(value = "CREATE", entity = "class_users", description = "Thêm học sinh vào lớp học")
    public void addStudentToClass(AddStudentToClassDTO dto) {
        // Kiểm tra xem đã tồn tại chưa
        if (classUserRepository.existsByClassField_IdAndStudent_Id(dto.getClassId(), dto.getStudentId())) {
            throw new RuntimeException("Học sinh đã có trong lớp này!");
        }

        ClassEntity clazz = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại"));

        Users student = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Học sinh không tồn tại"));

        ClassUser member = new ClassUser();
        ClassUserId id = new ClassUserId();
        id.setClassId(dto.getClassId());
        id.setStudentId(dto.getStudentId());

        member.setId(id);
        member.setClassField(clazz);
        member.setStudent(student);
        member.setJoinedAt(Instant.now());

        classUserRepository.save(member);

        // Xóa code ghi logs thủ công
        // activityLogService.log(...);
    }


    private ClassResponseDTO toDTO(ClassEntity clazz) {
        ClassResponseDTO dto = new ClassResponseDTO();
        dto.setId(clazz.getId());
        dto.setClassName(clazz.getClassName());
        dto.setSchoolYear(clazz.getSchoolYear());
        dto.setSemester(clazz.getSemester());
        dto.setDescription(clazz.getDescription());
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
        return dto;
    }

    public List<ClassMemberDTO> getStudentsInClass(Integer classId) {
        List<ClassUser> members = classUserRepository.findByClassField_Id(classId);

        return members.stream()
                .map(member -> {
                    Users student = member.getStudent();
                    ClassMemberDTO dto = new ClassMemberDTO();
                    dto.setId(student.getId());
                    dto.setFullName(student.getFullName());
                    dto.setUsername(student.getUsername());
                    dto.setEmail(student.getEmail());
                    dto.setJoinedAt(member.getJoinedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    public List<ClassResponseDTO> getClassesOfStudent(Integer studentId) {
        List<ClassUser> members = classUserRepository.findByStudent_Id(studentId);

        return members.stream()
                .map(member -> toDTO(member.getClassField()))
                .collect(Collectors.toList());
    }

    public PaginatedClassResponseDto getClassesOfStudent(Integer studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ClassUser> membersPage = classUserRepository.findByStudent_Id(studentId, pageable);

        List<ClassResponseDTO> classDTOs = membersPage.getContent().stream()
                .map(member -> toDTO(member.getClassField()))
                .collect(Collectors.toList());

        PaginatedClassResponseDto response = new PaginatedClassResponseDto();
        response.setData(classDTOs);
        response.setPageNumber(membersPage.getNumber());
        response.setPageSize(membersPage.getSize());
        response.setTotalRecords(membersPage.getTotalElements());
        response.setTotalPages(membersPage.getTotalPages());
        response.setHasNext(membersPage.hasNext());
        response.setHasPrevious(membersPage.hasPrevious());

        return response;
    }
    public List<ClassResponseDTO> getClassesOfTeacher(Integer teacherId) {
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
}