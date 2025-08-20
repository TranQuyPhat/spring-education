package com.example.springboot_education.dtos.gradeDTOs.GradeBase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeightedScorePerClassDTO {
    private Integer classId;
    private String className;
    private String studentName;
    private Double quizAvg;       // nullable
    private Double assignmentAvg; // nullable
    private Double totalScore;    // đã tính theo hệ số
}