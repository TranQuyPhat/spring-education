package com.example.springboot_education.dtos.quiz.base;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class QuizBaseDTO {
    protected Integer id;
    protected String title;
    protected String description;
    protected Integer timeLimit;
    protected LocalDate startDate;
    protected LocalDate endDate;
    protected String grade;
    protected String subject;
}
