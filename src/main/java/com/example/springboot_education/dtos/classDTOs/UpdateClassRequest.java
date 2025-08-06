package com.example.springboot_education.dtos.classDTOs;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateClassRequest {
    @NotBlank
    private String className;

    // @NotBlank
    // private String subject;

    @NotNull
    private Integer schoolYear;

    @NotBlank
    private String semester;

    private String description;

    @NotNull
    private Instant updatedAt;
}


