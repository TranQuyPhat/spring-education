package com.example.springboot_education.dtos.classDTOs;

import com.example.springboot_education.entities.ClassEntity.JoinMode;

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
    private JoinMode joinMode; // AUTO or APPROVAL
}
