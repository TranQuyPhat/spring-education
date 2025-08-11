package com.example.springboot_education.dtos.gradeDTOs;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentOverviewDTO {
    private BigDecimal avgScore;
    private CountDto assignments;
    private CountDto quizzes;
    private List<ScoreRangeDTO> scoreDistribution;
}
