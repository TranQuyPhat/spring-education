package com.example.springboot_education.dtos.gradeDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentRankingDTO {
    private Integer studentId;
    private String fullName;
    private BigDecimal avgScore;
    private Integer rank;
}