// ClassService.java
package com.example.springboot_education.services.classes;


import com.example.springboot_education.dtos.classDTOs.AddStudentToClassDTO;
import com.example.springboot_education.dtos.classDTOs.ClassMemberDTO;
import com.example.springboot_education.dtos.classDTOs.ClassResponseDTO;
import com.example.springboot_education.dtos.classDTOs.CreateClassDTO;
import com.example.springboot_education.dtos.classDTOs.SubjectDTO;
import com.example.springboot_education.dtos.classDTOs.TeacherDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.ClassUser;
import com.example.springboot_education.entities.ClassUserId;
import com.example.springboot_education.entities.Subject;
// import com.example.springboot_education.entities.ClassMember;
import com.example.springboot_education.entities.Users;
// import com.example.springboot_education.repositories.ClassMemberRepository;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.ClassUserRepository;
import com.example.springboot_education.repositories.SubjectRepository;
import com.example.springboot_education.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UserRepository userRepository;
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

    public ClassResponseDTO createClass(CreateClassDTO dto) {
        Users teacher = userRepository.findById(dto.getTeacherId()).orElseThrow();
        Subject subject = subjectRepository.findById(dto.getSubjectId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học"));
        
        ClassEntity clazz = new ClassEntity();
        clazz.setClassName(dto.getClassName());
        clazz.setSchoolYear(dto.getSchoolYear());
        clazz.setSemester(dto.getSemester());
        clazz.setDescription(dto.getDescription());
        clazz.setTeacher(teacher);
        clazz.setSubject(subject);
        clazz.setCreatedAt(Instant.now());

        return toDTO(classRepository.save(clazz));
    }

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

        return toDTO(classRepository.save(clazz));
    }

    public void deleteClass(Integer id) {
        classRepository.deleteById(id);
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
    }
}
