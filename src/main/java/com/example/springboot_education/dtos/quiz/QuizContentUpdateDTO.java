package com.example.springboot_education.dtos.quiz;

import com.example.springboot_education.dtos.quiz.teacher.QuestionTeacherDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class QuizContentUpdateDTO {
    @Valid
    @NotEmpty(message = "Quiz must have at least one question")
    private List<QuestionTeacherDTO> questions;
    private boolean replaceAll;
}
