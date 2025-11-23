package com.example.springboot_education.dtos.quiz;

import lombok.Data;

import java.time.Instant;

@Data
public class QuizSummaryDTO {
    private Integer id;
    private String title;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private String subject;
    private Integer totalQuestions;
}
