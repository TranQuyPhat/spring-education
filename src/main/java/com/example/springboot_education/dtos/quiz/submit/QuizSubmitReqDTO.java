package com.example.springboot_education.dtos.quiz.submit;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
@Data
public class QuizSubmitReqDTO {
    private Integer quizId;
    private Instant startAt;
    private Instant endAt;
    private Map<Integer, List<String>> answers;
}
