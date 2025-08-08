package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.materialDTOs.ClassMaterialRequestDto;
import com.example.springboot_education.dtos.materialDTOs.ClassMaterialResponseDto;
import com.example.springboot_education.services.material.ClassMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/materials")
public class ClassMaterialController {
    private final ClassMaterialService materialService;

    @PostMapping
    public ResponseEntity<ClassMaterialResponseDto> create(@RequestBody @Valid ClassMaterialRequestDto dto) {
        return ResponseEntity.ok(materialService.createMaterial(dto));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ClassMaterialResponseDto>> getByClass(@PathVariable Integer classId) {
        return ResponseEntity.ok(materialService.getMaterialsByClass(classId));
    }

    @PostMapping("/{id}/download")
    public ResponseEntity<Void> increaseDownload(@PathVariable Integer id) {
        materialService.increaseDownloadCount(id);
        return ResponseEntity.ok().build();
    }
}

