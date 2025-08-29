package com.example.springboot_education.dtos.classDTOs;

import java.time.Instant;

import com.example.springboot_education.entities.ClassEntity.JoinMode;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateClassRequest {
    private String className;

    private Integer schoolYear;

    private String semester;

    private String description;

    private Integer subjectId;

    private JoinMode joinMode;

    @NotNull
    private Instant updatedAt;
}


