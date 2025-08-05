package com.example.springboot_education.dtos.quiz;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class QuizResponseDTO {
    protected Integer id;
    protected String title;
    protected String description;
    protected Integer timeLimit;
    protected LocalDate startDate;
    protected LocalDate endDate;
    protected String grade;
    protected String subject;
}
