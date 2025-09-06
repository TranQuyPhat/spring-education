package com.example.springboot_education.dtos.quiz.base;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
public class QuizBaseDTO {
    protected Integer id;
    @NotBlank(message = "Title is required")
    protected String title;
    protected String description;
    @Positive(message = "Time limit must be greater than 0")
    protected Integer timeLimit;
    @NotNull(message = "Start date is required")
    protected LocalDate startDate;
    @NotNull(message = "End date is required")
    protected LocalDate endDate;
    protected String grade;
    protected String subject;
}
