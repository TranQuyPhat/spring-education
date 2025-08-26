package com.example.springboot_education.dtos.subjects;


import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectResponseDTO {
    private Integer id;
    private String subjectName;
    private String description;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;

}