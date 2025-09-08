package com.example.springboot_education.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.springboot_education.dtos.materialDTOs.ClassMaterialRequestDto;
import com.example.springboot_education.dtos.materialDTOs.ClassMaterialResponseDto;
import com.example.springboot_education.dtos.materialDTOs.DownloadFileDTO;
import com.example.springboot_education.services.material.ClassMaterialService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/materials")
public class ClassMaterialController {
    private final ClassMaterialService materialService;

    // Create a new material with file upload
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClassMaterialResponseDto> create(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("classId") Integer classId,
            @RequestParam("file") MultipartFile file) throws IOException {

        ClassMaterialRequestDto dto = new ClassMaterialRequestDto();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setClassId(classId);

        return ResponseEntity.ok(materialService.createMaterial(dto, file));
    }

    // Update an existing material with optional file upload
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClassMaterialResponseDto> update(
            @PathVariable("id") Integer id,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws IOException {
        ClassMaterialRequestDto dto = new ClassMaterialRequestDto();
        dto.setTitle(title);
        dto.setDescription(description);

        return ResponseEntity.ok(materialService.updateMaterial(id, dto, file));
    }

    // Delete a material by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }

    // Get materials by classId
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ClassMaterialResponseDto>> getByClass(@PathVariable("classId") Integer classId) {
        return ResponseEntity.ok(materialService.getMaterialsByClass(classId));
    }

    // Get all materials
    @GetMapping
    public List<ClassMaterialResponseDto> getAllMaterials() {
        return materialService.getAllMaterials();
    }

    // Download file
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadMaterial(@PathVariable("id") Integer id) throws Exception {
        DownloadFileDTO fileDto = materialService.downloadMaterial(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileDto.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.getFileName() + "\"")
                .body(fileDto.getResource());
    }
}
