package com.example.springboot_education.dtos.quiz;

import lombok.Data;

import java.util.List;

@Data
public class TeacherOverviewDTO {
    private Integer teacherId;
    private List<ClassOverviewDTO> classes;
}
