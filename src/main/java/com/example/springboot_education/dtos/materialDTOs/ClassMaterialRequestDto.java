package com.example.springboot_education.dtos.materialDTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassMaterialRequestDto {
    @NotNull
    private String title;

    private String description;

    @NotNull
    private String filePath;

    private String fileType;

    @NotNull
    private Integer createdBy;

    @NotNull
    private Integer classId;
}