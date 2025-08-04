package com.example.springboot_education.dtos.finalgrades;

import lombok.Data;

@Data
public class FinalGradeUpdateDTO {
    private Double averageScore;
    private String letterGrade;
}
