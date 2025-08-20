package com.example.springboot_education.dtos.gradeDTOs.GradeBase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAverageScoreDTO {
    private Integer quizId;
    private String studentName;
    private Double averageScore;
}