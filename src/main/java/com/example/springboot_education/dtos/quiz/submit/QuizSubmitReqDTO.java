package com.example.springboot_education.dtos.quiz.submit;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
public class QuizSubmitReqDTO {
    @NotNull(message = "Quiz ID is required")
    private Integer quizId;

    @NotNull(message = "Student ID is required")
    private Integer studentId;

    private Instant startAt;
    private Instant endAt;
    private Instant submittedAt;

    @NotNull(message = "Answers cannot be null")
    @NotEmpty(message = "Answers cannot be empty")
    private Map<Integer, List<String>> answers;
}
