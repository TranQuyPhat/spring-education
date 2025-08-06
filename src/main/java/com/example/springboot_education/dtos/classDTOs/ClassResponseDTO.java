package com.example.springboot_education.dtos.classDTOs;

import lombok.Data;


import java.time.Instant;

@Data
public class ClassResponseDTO {
    private Integer id;
    private String className;
    // private String subject;
    private Integer schoolYear;
    private String semester;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer teacherId;
}
