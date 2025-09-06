package com.example.springboot_education.dtos.quiz;

import com.example.springboot_education.dtos.quiz.teacher.QuestionTeacherDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class QuizRequestDTO {
    @NotBlank(message = "Quiz title is required")
    private String title;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String grade;

    private String description;

    @Min(value = 1, message = "Time limit must be greater than 0")
    private Integer timeLimit; 

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotNull(message = "Class ID is required")
    private Integer classId;

    @NotNull(message = "Creator ID is required")
    private Integer createdBy;
    @Valid
    @NotEmpty
    private List<QuestionTeacherDTO> questions;
}

