package com.example.springboot_education.dtos.quiz;

import lombok.Data;

import java.util.List;

@Data
public class ClassOverviewDTO {
    private Integer classId;
    private String className;
    private List<QuizSummaryDTO> latestQuizzes;
    private Integer totalQuizzes;
}
