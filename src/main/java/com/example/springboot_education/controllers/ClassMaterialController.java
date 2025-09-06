package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.materialDTOs.ClassMaterialRequestDto;
import com.example.springboot_education.dtos.materialDTOs.ClassMaterialResponseDto;
import com.example.springboot_education.dtos.materialDTOs.DownloadFileDTO;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.services.material.ClassMaterialService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    // @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // public ResponseEntity<ClassMaterialResponseDto> create(
    // @RequestParam("title") @NotBlank String title,
    // @RequestParam(value = "description", required = false) String description,
    // @RequestParam("classId") @NotNull Integer classId,
    // @RequestParam("createdBy") @NotNull Integer createdBy,
    // @RequestParam("file") MultipartFile file) throws IOException {

    // if (file == null || file.isEmpty()) {
    // throw new HttpException("File must not be empty", HttpStatus.BAD_REQUEST);
    // }

    // ClassMaterialRequestDto dto = new ClassMaterialRequestDto();
    // dto.setTitle(title);
    // dto.setDescription(description);
    // dto.setClassId(classId);
    // dto.setCreatedBy(createdBy);

    // return ResponseEntity.ok(materialService.createMaterial(dto, file));
    // }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClassMaterialResponseDto> create(
            @Valid ClassMaterialRequestDto dto,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new HttpException("File must not be empty", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(materialService.createMaterial(dto, file));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ClassMaterialResponseDto>> getByClass(@PathVariable("classId") Integer classId) {
        return ResponseEntity.ok(materialService.getMaterialsByClass(classId));
    }

    @GetMapping()
    public List<ClassMaterialResponseDto> getAllMaterials() {
        return materialService.getAllMaterials();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadMaterial(@PathVariable("id") Integer id) throws Exception {
        DownloadFileDTO fileDto = materialService.downloadMaterial(id);

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(fileDto.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.getFileName() + "\"")
                .body(fileDto.getResource());
    }
}
