package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.materialDTOs.ClassMaterialRequestDto;
import com.example.springboot_education.dtos.materialDTOs.ClassMaterialResponseDto;
import com.example.springboot_education.dtos.materialDTOs.DownloadFileDTO;
import com.example.springboot_education.services.material.ClassMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClassMaterialResponseDto> create(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("classId") Integer classId,
            @RequestParam("createdBy") Integer createdBy,
            @RequestParam("file") MultipartFile file) throws IOException {

        ClassMaterialRequestDto dto = new ClassMaterialRequestDto();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setClassId(classId);
        dto.setCreatedBy(createdBy);

        return ResponseEntity.ok(materialService.createMaterial(dto, file));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ClassMaterialResponseDto>> getByClass(@PathVariable Integer classId) {
        return ResponseEntity.ok(materialService.getMaterialsByClass(classId));
    }

//    @PostMapping("/{id}/download")
//    public ResponseEntity<Void> increaseDownload(@PathVariable Integer id) {
//        materialService.increaseDownloadCount(id);
//        return ResponseEntity.ok().build();
//    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadMaterial(@PathVariable Integer id) throws Exception {
        DownloadFileDTO fileDto = materialService.downloadMaterial(id);

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(fileDto.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.getFileName() + "\"")
                .body(fileDto.getResource());
    }
}

