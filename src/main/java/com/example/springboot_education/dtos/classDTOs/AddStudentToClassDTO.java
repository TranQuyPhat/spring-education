package com.example.springboot_education.dtos.classDTOs;


import lombok.Data;

@Data
public class AddStudentToClassDTO {
    private Integer classId;
    private Integer studentId;
}
