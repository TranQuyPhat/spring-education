package com.example.springboot_education.controllers.classes;



import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
// import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.springboot_education.dtos.classDTOs.AddStudentToClassDTO;
import com.example.springboot_education.dtos.classDTOs.ClassMemberDTO;
import com.example.springboot_education.dtos.classDTOs.ClassResponseDTO;
import com.example.springboot_education.dtos.classDTOs.CreateClassDTO;
import com.example.springboot_education.dtos.classDTOs.PaginatedClassResponseDto;
import com.example.springboot_education.services.classes.ClassService;

import java.util.List;

@RestController
@RequestMapping("/api/auth/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @GetMapping
    public List<ClassResponseDTO> getAllClasses() {
        return classService.getAllClasses();
    }

    @GetMapping("/{id}")
    public ClassResponseDTO getClassById(@PathVariable("id") Integer id) {
        return classService.getClassById(id);
    }

    @PostMapping
    public ClassResponseDTO createClass(@RequestBody CreateClassDTO dto) {
        return classService.createClass(dto);
    }

    @PutMapping("/{id}")
    public ClassResponseDTO updateClass(@PathVariable("id") Integer id, @RequestBody CreateClassDTO dto) {
        return classService.updateClass(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteClass(@PathVariable("id") Integer id) {
        classService.deleteClass(id);
    }
    @GetMapping("/{classId}/students")
    public ResponseEntity<List<ClassMemberDTO>> getStudentsInClass(@PathVariable("classId") Integer classId) {
        List<ClassMemberDTO> students = classService.getStudentsInClass(classId);
        return ResponseEntity.ok(students);
    }
    @GetMapping("/students/{studentId}/classes")
    public ResponseEntity<List<ClassResponseDTO>> getClassesOfStudent(@PathVariable("studentId") Integer studentId) {
        List<ClassResponseDTO> classes = classService.getClassesOfStudent(studentId);
        return ResponseEntity.ok(classes);
    }
    @PostMapping("/add-student")
    public ResponseEntity<?> addStudentToClass(@RequestBody AddStudentToClassDTO dto) {
        classService.addStudentToClass(dto);
        return ResponseEntity.ok("Thêm học sinh vào lớp thành công");
    }
    @GetMapping("/teacher/{teacherId}")
    public PaginatedClassResponseDto getClassesOfTeacher(
            @PathVariable("teacherId") Integer teacherId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size
    ) {
        return classService.getClassesOfTeacher(teacherId, page, size);
    }

    @GetMapping("/student/{studentId}/classesPaginated")
    public ResponseEntity<PaginatedClassResponseDto> getStudentClasses(
            @PathVariable("studentId") Integer studentId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size
    ) {
        PaginatedClassResponseDto response = classService.getClassesOfStudent(studentId, page, size);
        return ResponseEntity.ok(response);
    }
}
