// ClassService.java
package com.example.springboot_education.services.classes;

import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO;
import com.example.springboot_education.dtos.classDTOs.*;
import com.example.springboot_education.entities.*;
import org.springframework.transaction.annotation.Transactional;

import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import com.example.springboot_education.entities.ClassEntity.JoinMode;
import com.example.springboot_education.entities.ClassUser;
import com.example.springboot_education.entities.ClassUserId;
import com.example.springboot_education.entities.Subject;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.SubjectRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.classes.ClassUserRepository;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UsersJpaRepository userRepository;
    private final ClassUserRepository classUserRepository;
    private final SubjectRepository subjectRepository;

    public List<ClassResponseDTO> getAllClasses() {
        return classRepository.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClassResponseDTO getClassById(Integer id) {
        ClassEntity clazz = classRepository.findById(id).orElseThrow();
        return toDTO(clazz);
    }
    public ClassEntity getClassEntityById(Integer id) {
    return classRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + id));
}
    @Transactional
    @LoggableAction(value = "CREATE", entity = "classes", description = "Tạo lớp học mới")
    @CacheEvict(value = "classesOfTeacher", key = "#dto.teacherId")
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
        clazz.setJoinMode(dto.getJoinMode() != null ? dto.getJoinMode() : JoinMode.AUTO);
        clazz.setCreatedAt(Instant.now());

        ClassEntity saved = classRepository.save(clazz);

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
        clazz.setJoinMode(dto.getJoinMode() != null ? dto.getJoinMode() : clazz.getJoinMode());
        clazz.setUpdatedAt(Instant.now());

        ClassEntity updated = classRepository.save(clazz);

        return toDTO(updated);
    }

    @LoggableAction(value = "DELETE", entity = "classes", description = "Xóa lớp học")
    public void deleteClass(Integer id) {
        ClassEntity clazz = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại"));

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
        return dto;
    }
    @Cacheable(value = "classesOfTeacher", key = "#teacherId")
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


    //websocket
    public Integer getTeacherIdOfClass(Integer classId) {
        return classRepository.findById(classId).map(c -> c.getTeacher().getId()) // lấy id từ quan hệ
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp với ID: " + classId));
    }


    public String getClassName(Integer classId) {
        return classRepository.findById(classId)
                .map(ClassEntity::getClassName)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp với ID: " + classId));
    }

}
