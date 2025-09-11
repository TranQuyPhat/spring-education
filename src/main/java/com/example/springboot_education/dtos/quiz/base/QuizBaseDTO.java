package com.example.springboot_education.dtos.quiz.base;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class QuizBaseDTO {
    protected Integer id;
    protected String title;
    protected String description;
    protected Integer timeLimit;
    protected OffsetDateTime startDate;
    protected OffsetDateTime endDate;
    protected String subject;
    protected String className;
    protected int totalQuestion;
}

