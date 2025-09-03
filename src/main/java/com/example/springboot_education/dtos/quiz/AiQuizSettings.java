package com.example.springboot_education.dtos.quiz;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiQuizSettings {

    @NotBlank(message = "Mode is required")
    private String mode;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Question type is required")
    private String questionType;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @NotBlank(message = "Visibility is required")
    private String visibility;

    private String studyMode;

    private String aiLevel;

    @NotNull(message = "Number of questions is required")
    @Min(value = 1, message = "At least 1 question is required")
    @Max(value = 100, message = "Maximum 100 questions allowed")
    private Integer numQuestions;

    @NotBlank(message = "Quiz title cannot be blank")
    private String quizTitle;

    private String userPrompt;
}