package com.example.springboot_education.dtos.classDTOs;

import com.example.springboot_education.entities.ClassEntity.JoinMode;
import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class CreateClassDTO {
    @NotBlank(message = "Class name is required")
    @Size(max = 100, message = "Class name must not exceed 100 characters")
    private String className;

    @NotNull(message = "School year is required")
    @Min(value = 2000, message = "School year must be >= 2000")
    @Max(value = 2100, message = "School year must be <= 2100")
    private Integer schoolYear;

    @NotBlank(message = "Semester is required")
    @Size(max = 50, message = "Semester must not exceed 50 characters")
    private String semester;

    private String description;

    @NotNull(message = "Teacher ID is required")
    private Integer teacherId;

    @NotNull(message = "Subject ID is required")
    private Integer subjectId;

    @NotNull(message = "Join mode is required")
    private JoinMode joinMode; 
}
