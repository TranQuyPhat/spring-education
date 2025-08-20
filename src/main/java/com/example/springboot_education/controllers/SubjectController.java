package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.subjects.CreateSubjectDTO;
import com.example.springboot_education.dtos.subjects.SubjectResponseDTO;
import com.example.springboot_education.dtos.subjects.UpdateSubjectDTO;
import com.example.springboot_education.services.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    public ResponseEntity<List<SubjectResponseDTO>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> getSubject(@PathVariable Integer id) {
        return ResponseEntity.ok(subjectService.findById(id));
    }

    @PostMapping
    public ResponseEntity<SubjectResponseDTO> createSubject(@RequestBody CreateSubjectDTO dto) {
        return ResponseEntity.ok(subjectService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> updateSubject(@PathVariable("id") Integer id, @RequestBody UpdateSubjectDTO dto) {
        return ResponseEntity.ok(subjectService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable("id") Integer id) {
        subjectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

