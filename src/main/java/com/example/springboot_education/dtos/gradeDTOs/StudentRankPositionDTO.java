package com.example.springboot_education.dtos.gradeDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentRankPositionDTO {
    private Integer studentId;
    private Integer rank;
    private long totalStudents;
    private BigDecimal avgScore;
}
