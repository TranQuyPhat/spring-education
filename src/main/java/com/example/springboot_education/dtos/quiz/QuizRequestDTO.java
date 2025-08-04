package com.example.springboot_education.dtos.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
public class QuizRequestDTO {
    @NotBlank
    private String title;

    private String subject;
    private String grade;
    private String description;
    private Integer timeLimit;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer classId;
    private Long createdBy;

    @Valid
    @NotEmpty
    private List<QuestionDTO> questions;
}

