package com.example.springboot_education.dtos.gradeDTOs.GradeBase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseScoreStatsDTO {
    private Integer classId;
    private String className;
    private String studentName;
    private String studentEmail;
    private Double averageScore;
}