package com.example.springboot_education.dtos.quiz;

import lombok.Data;

import java.time.Instant;
import java.util.Map;
@Data
public class QuizSubmitReqDTO {
    private Integer quizId;
    private Integer studentId;
    private Instant startAt;
    private Instant endAt;
    private Instant submittedAt;
    private Map<Integer, String> answers;
}
