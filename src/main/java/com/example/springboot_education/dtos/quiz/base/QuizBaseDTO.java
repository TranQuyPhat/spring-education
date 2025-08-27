package com.example.springboot_education.dtos.quiz.base;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class QuizBaseDTO {
    protected Integer id;
    protected String title;
    protected String description;
    protected Integer timeLimit;
    protected LocalDateTime startDate;
    protected LocalDateTime endDate;
    protected String grade;
    protected String subject;
    protected String className;
}
