package com.example.springboot_education.dtos.gradeDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizDetailDTO {
    private Integer quizId;
    private String title;
    private BigDecimal score;
    private Timestamp submittedAt;
}
