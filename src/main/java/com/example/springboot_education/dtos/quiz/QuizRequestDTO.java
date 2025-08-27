package com.example.springboot_education.dtos.quiz;

import com.example.springboot_education.dtos.quiz.teacher.QuestionTeacherDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class QuizRequestDTO {
    @NotBlank
    private String title;
    private String subject;
    private String grade;
    private String description;
    private Integer timeLimit;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer classId;
    private Integer createdBy;
    @Valid
    @NotEmpty
    private List<QuestionTeacherDTO> questions;
}

