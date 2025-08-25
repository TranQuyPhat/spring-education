// ClassService.java
package com.example.springboot_education.services.classes;


import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO;
import com.example.springboot_education.dtos.classDTOs.AddStudentToClassDTO;
import com.example.springboot_education.dtos.classDTOs.ClassMemberDTO;
import com.example.springboot_education.dtos.classDTOs.ClassResponseDTO;
import com.example.springboot_education.dtos.classDTOs.PaginatedClassResponseDto;
import com.example.springboot_education.dtos.classDTOs.SubjectDTO;
import com.example.springboot_education.dtos.classDTOs.TeacherDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.ClassUser;
import com.example.springboot_education.entities.ClassUserId;
// import com.example.springboot_education.entities.ClassMember;
import com.example.springboot_education.entities.Users;
// import com.example.springboot_education.repositories.ClassMemberRepository;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.SubjectRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.classes.ClassUserRepository;
import com.example.springboot_education.services.ActivityLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

// import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassUserService {
    private final ClassRepository classRepository;
    private final UsersJpaRepository userRepository;
    private final ClassUserRepository classUserRepository;
    private final ActivityLogService activityLogService;



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
                .map(member -> toDTO(member.getClassField())) // tái sử dụng toDTO
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

    //websocket
    public void addStudentToClass(Integer classId, Integer studentId) {
    AddStudentToClassDTO dto = new AddStudentToClassDTO();
    dto.setClassId(classId.intValue());  // Nếu classId là int
    dto.setStudentId(studentId.intValue());
    this.addStudentToClass(dto);
    }



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

        member.setId(id); // gán EmbeddedId
        member.setClassField(clazz);
        member.setStudent(student);
        member.setJoinedAt(Instant.now()); // không cần Timestamp.from()

        classUserRepository.save(member);

        // Ghi log ADD STUDENT
        activityLogService.log(new ActivityLogCreateDTO(
                "CREATE",
                dto.getClassId(),
                "class_users",
                "Thêm học sinh " + student.getFullName() + " vào lớp " + clazz.getClassName(),
                student.getId()
        ));
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
        // dto.setTeacherName(clazz.getTeacher() != null ? clazz.getTeacher().getFullName() : null);
        // dto.setSubjectName(clazz.getSubject() != null ? clazz.getSubject().getSubjectName() : null);


        if (clazz.getTeacher() != null) {
            TeacherDTO teacherDTO = new TeacherDTO();
            teacherDTO.setId(clazz.getTeacher().getId());
            teacherDTO.setFullName(clazz.getTeacher().getFullName());
            dto.setTeacher(teacherDTO);
        }

    // Gán subject
        if (clazz.getSubject() != null) {
            SubjectDTO subjectDTO = new SubjectDTO();
            subjectDTO.setId(clazz.getSubject().getId());
            subjectDTO.setName(clazz.getSubject().getSubjectName());
            dto.setSubject(subjectDTO);
        }
        return dto;
    }   
}
