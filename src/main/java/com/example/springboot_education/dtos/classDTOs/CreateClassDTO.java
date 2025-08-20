package com.example.springboot_education.dtos.classDTOs;

import lombok.Data;

@Data
public class CreateClassDTO {
    private String className;
    // private String subject;
    private Integer schoolYear;
    private String semester;
    private String description;
    private Integer teacherId;
    private Integer subjectId;
}
