package com.example.springboot_education.dtos.quiz.submit;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
@Getter
@Setter
public class QuizSubmissionBaseDTO {
    private Integer id;
    private Integer quizId;
    private String quizTitle;
    private String subjectName;
    private String className;
    private Integer studentId;
    private String studentName;
    private BigDecimal score;
    private Instant submittedAt;
    private Instant startAt;
    private Instant endAt;
    private Instant gradedAt;
}
