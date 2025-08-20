package com.example.springboot_education.dtos.quiz;

import lombok.Data;

@Data
public class AiQuizSettings {
    private String mode;
    private String language;
    private String questionType;
    private String difficulty;
    private String visibility;
    private String studyMode;
    private String aiLevel;
    private Integer numQuestions;
    private String quizTitle;
    private String userPrompt;
}