package com.example.springboot_education.dtos.quiz;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
@Data
public class QuizSubmitResDTO {
        private Integer id;
        private Integer quizId;
        private Integer studentId;
        private String studentName;
        private String className;          // VD: 12A1
        private String subjectName;        // VD: Toán học
        private Integer durationMinutes;   // VD: 45
        private BigDecimal score;          // VD: 8.00
        private Instant gradedAt;
        private Instant startAt;
        private Instant endAt;
    private String quizTitle;
}
