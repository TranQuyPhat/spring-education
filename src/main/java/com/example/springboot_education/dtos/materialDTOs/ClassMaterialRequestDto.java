package com.example.springboot_education.dtos.materialDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassMaterialRequestDto {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String filePath;

    private String fileType;

    @NotNull(message = "CreatedBy is required")
    private Integer createdBy;

    @NotNull(message = "ClassId is required")
    private Integer classId;
}