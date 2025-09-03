package com.example.springboot_education.dtos.classDTOs;

import java.time.Instant;

import com.example.springboot_education.entities.ClassEntity.JoinMode;
import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class UpdateClassRequest {
    @Size(max = 100, message = "Class name must not exceed 100 characters")
    private String className;

    @Min(value = 2000, message = "School year must be >= 2000")
    @Max(value = 2100, message = "School year must be <= 2100")
    private Integer schoolYear;

    @Size(max = 50, message = "Semester must not exceed 50 characters")
    private String semester;

    private String description;

    private Integer subjectId;

    private JoinMode joinMode;

    @NotNull(message = "updatedAt is required")
    private Instant updatedAt;
}
