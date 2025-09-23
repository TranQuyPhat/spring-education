package com.example.springboot_education.controllers.classes;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
// import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.springboot_education.dtos.classDTOs.ClassResponseDTO;
import com.example.springboot_education.dtos.classDTOs.CreateClassDTO;
import com.example.springboot_education.dtos.classDTOs.PaginatedClassResponseDto;
import com.example.springboot_education.services.classes.ClassService;

import jakarta.validation.Valid;

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
    public ClassResponseDTO createClass(@Valid @RequestBody CreateClassDTO dto) {
        return classService.createClass(dto);
    }

    @PutMapping("/{id}")
    public ClassResponseDTO updateClass(@PathVariable("id") Integer id, @Valid @RequestBody CreateClassDTO dto) {
        return classService.updateClass(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteClass(@PathVariable("id") Integer id) {
        classService.deleteClass(id);
    }

    @GetMapping("/teachers/{teacherId}")
    public List<ClassResponseDTO> getAllClassesOfTeacher(
            @PathVariable("teacherId") Integer teacherId) {
        return classService.getAllClassesOfTeacher(teacherId);
    }

    @GetMapping("/teacher/{teacherId}")
    public PaginatedClassResponseDto getClassesOfTeacher(
            @PathVariable("teacherId") Integer teacherId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {
        return classService.getClassesOfTeacher(teacherId, page, size);
    }

    @PostMapping("/test")
    public String test(@Valid @RequestBody CreateClassDTO dto) {
        return "OK - TeacherId: " + dto.getTeacherId();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClassResponseDTO>> searchClasses(
            @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        return ResponseEntity.ok(classService.searchClasses(keyword));
    }

    // API lấy 10 lớp mới nhất
    @GetMapping("/latest")
    public ResponseEntity<List<ClassResponseDTO>> getLatestClasses() {
        return ResponseEntity.ok(classService.getLatestClasses());
    }

    @GetMapping("/teacher/{teacherId}/searchPaginate")
    public ResponseEntity<PaginatedClassResponseDto> searchClassesPaginate(
            @PathVariable("teacherId") Integer teacherId,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size
    ) {
        return ResponseEntity.ok(classService.searchClassesPaginate(teacherId, keyword, page, size));
    }

}
