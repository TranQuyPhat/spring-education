package com.example.springboot_education.dtos.quiz;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class QuizResponseDTO {
    private Integer id;
    private String title;
    private String description;
    private Integer timeLimit;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer classId;
    private Integer createdBy;
    private String grade;
    private String subject;
    private List<QuestionResponseDTO> questions;
    private String className;
    private int totalStudents;
    private int studentsSubmitted;
}
