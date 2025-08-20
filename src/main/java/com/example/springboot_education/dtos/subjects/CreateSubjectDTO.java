package com.example.springboot_education.dtos.subjects;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSubjectDTO {
    private String subjectName;
    private String description;
    private Integer createdById;
}