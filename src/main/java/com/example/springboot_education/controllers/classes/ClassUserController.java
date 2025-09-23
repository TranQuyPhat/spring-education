package com.example.springboot_education.controllers.classes;



import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
// import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.springboot_education.dtos.classDTOs.AddStudentToClassDTO;
import com.example.springboot_education.dtos.classDTOs.ClassMemberDTO;
import com.example.springboot_education.dtos.classDTOs.ClassResponseDTO;
import com.example.springboot_education.dtos.classDTOs.PaginatedClassResponseDto;
import com.example.springboot_education.services.classes.ClassUserService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/classes")
@RequiredArgsConstructor
public class ClassUserController {
    private final ClassUserService classUserService;

    @GetMapping("/{classId}/students")
    public ResponseEntity<List<ClassMemberDTO>> getStudentsInClass(@PathVariable("classId") Integer classId) {
        List<ClassMemberDTO> students = classUserService.getStudentsInClass(classId);
        return ResponseEntity.ok(students);
    }
    @GetMapping("/students/{studentId}/classes")
    public ResponseEntity<List<ClassResponseDTO>> getClassesOfStudent(@PathVariable("studentId") Integer studentId) {
        List<ClassResponseDTO> classes = classUserService.getClassesOfStudent(studentId);
        return ResponseEntity.ok(classes);
    }
    @PostMapping("/add-student")
    public ResponseEntity<?> addStudentToClass(@Valid @RequestBody AddStudentToClassDTO dto) {
        classUserService.addStudentToClass(dto);
        return ResponseEntity.ok("Thêm học sinh vào lớp thành công");
    }
    @GetMapping("/student/{studentId}/classesPaginated")
    public ResponseEntity<PaginatedClassResponseDto> getStudentClasses(
            @PathVariable("studentId") Integer studentId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size
    ) {
        PaginatedClassResponseDto response = classUserService.getClassesOfStudent(studentId, page, size);
        return ResponseEntity.ok(response);
    }

    // Tính điểm chuyên cần cho cả lớp
    @PostMapping("/finalize/{classId}")
    public ResponseEntity<String> finalizeForClass(@PathVariable("classId") Integer classId) {
        classUserService.finalizeAttendanceScore(classId);
        return ResponseEntity.ok("Attendance score finalized for class " + classId);
    }

    @GetMapping("/{classId}/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentScore(
            @PathVariable("classId") Integer classId,
            @PathVariable("studentId") Integer studentId
    ) {
        Double score = classUserService.getAttendanceScore(classId, studentId);

        return ResponseEntity.ok(Map.of(
                "classId", classId,
                "studentId", studentId,
                "attendanceScore", score
        ));
    }

@GetMapping("/student/{studentId}/search")
public ResponseEntity<List<ClassResponseDTO>> searchClassesOfStudent(
        @PathVariable("studentId") Integer studentId,
        @RequestParam(name = "keyword", defaultValue = "") String keyword
) {
    return ResponseEntity.ok(classUserService.searchClassesOfStudent(studentId, keyword));
}
}
