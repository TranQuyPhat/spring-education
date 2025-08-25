package com.example.springboot_education.controllers.schedules;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.example.springboot_education.dtos.classschedules.ClassSchedulePatternCreateDTO;
import com.example.springboot_education.dtos.classschedules.ClassSchedulePatternResponseDTO;
import com.example.springboot_education.dtos.classschedules.ClassSchedulePatternUpdateDTO;

import com.example.springboot_education.services.schedules.ClassSchedulePatternService;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/class-schedule-patterns")
@RequiredArgsConstructor
public class ClassSchedulePatternController {

    private final ClassSchedulePatternService service;

    // Tạo nhiều pattern cho 1 lớp
    @PostMapping
    public ResponseEntity<List<ClassSchedulePatternResponseDTO>> createBatch(
            @RequestBody ClassSchedulePatternCreateDTO dto
    ) {
        return ResponseEntity.ok(service.createBatch(dto));
    }

    // Lấy toàn bộ pattern của 1 lớp
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ClassSchedulePatternResponseDTO>> getAllByClass(
            @PathVariable Integer classId
    ) {
        return ResponseEntity.ok(service.getAllByClass(classId));
    }

    // Lấy toàn bộ pattern (có phân trang)
    @GetMapping
    public ResponseEntity<Page<ClassSchedulePatternResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    // Cập nhật pattern
    @PutMapping("/batch")
    public List<ClassSchedulePatternResponseDTO> updateBatch(
            @RequestBody ClassSchedulePatternUpdateDTO dto) {
        return service.updateBatch(dto);
    }

    // Xóa pattern
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
