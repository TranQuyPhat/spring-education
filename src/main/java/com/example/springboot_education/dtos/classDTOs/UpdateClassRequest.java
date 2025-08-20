package com.example.springboot_education.dtos.classDTOs;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateClassRequest {
    @NotBlank
    private String className;

    @NotNull
    private Integer schoolYear;

    @NotBlank
    private String semester;

    private String description;

    private Integer subjectId;

    @NotNull
    private Instant updatedAt;
}


