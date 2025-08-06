package com.example.springboot_education.dtos.materialDTOs;

import lombok.Data;

import java.time.Instant;

@Data
public class ClassMaterialResponseDto {
    private Integer id;
    private String title;
    private String description;
    private String filePath;
    private String fileType;
    private String createdBy;
    private Integer classId;
    private Integer downloadCount;
    private Instant createdAt;
    private Instant updatedAt;
}
