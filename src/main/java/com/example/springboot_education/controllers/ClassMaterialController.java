package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.materialDTOs.ClassMaterialRequestDto;
import com.example.springboot_education.dtos.materialDTOs.ClassMaterialResponseDto;
import com.example.springboot_education.dtos.materialDTOs.DownloadFileDTO;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.services.material.ClassMaterialService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/materials")
public class ClassMaterialController {

    private final ClassMaterialService materialService;

    // Create new material
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClassMaterialResponseDto> create(
            @Valid ClassMaterialRequestDto dto,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new HttpException("File must not be empty", HttpStatus.BAD_REQUEST);
        }

        ClassMaterialResponseDto response = materialService.createMaterial(dto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Update existing material (file optional)
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

        ClassMaterialResponseDto updated = materialService.updateMaterial(id, dto, file);
        return ResponseEntity.ok(updated);
    }

    // Delete material by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }

    // Get materials by class
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ClassMaterialResponseDto>> getByClass(@PathVariable("classId") Integer classId) {
        List<ClassMaterialResponseDto> list = materialService.getMaterialsByClass(classId);
        return ResponseEntity.ok(list);
    }

    // Get all materials
    @GetMapping
    public ResponseEntity<List<ClassMaterialResponseDto>> getAllMaterials() {
        List<ClassMaterialResponseDto> list = materialService.getAllMaterials();
        return ResponseEntity.ok(list);
    }

    // Download file with error handling
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadMaterial(@PathVariable("id") Integer id) {
        try {
            DownloadFileDTO fileDto = materialService.downloadMaterial(id);
            if (fileDto == null || fileDto.getResource() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileDto.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.getFileName() + "\"")
                    .body(fileDto.getResource());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error downloading file: " + e.getMessage());
        }
    }
}
