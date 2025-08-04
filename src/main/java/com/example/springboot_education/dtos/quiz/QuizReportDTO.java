package com.example.springboot_education.dtos.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;



import java.math.BigDecimal;
import java.time.Instant;
@Data
@AllArgsConstructor
public class QuizReportDTO {
    private Integer quizId;
    private String quizTitle;
    private String quizStartDate;
    private String quizEndDate;
    private String subjectName;
    private String className;
    private Integer totalStudentsInClass;
    private Integer submissionId;
    private Integer studentId;
    private String studentName;
    private BigDecimal score;
    private Instant startAt;
    private Instant endAt;
    private Instant submittedAt;
    private Instant gradedAt;
}