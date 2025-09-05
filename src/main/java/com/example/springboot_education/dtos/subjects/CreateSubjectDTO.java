package com.example.springboot_education.dtos.subjects;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSubjectDTO {
    @NotNull(message = "Subject name must not be null")
    private String subjectName;
    private String description;
    private Integer createdById;
}