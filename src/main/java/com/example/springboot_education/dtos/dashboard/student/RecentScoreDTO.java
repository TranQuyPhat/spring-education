package com.example.springboot_education.dtos.dashboard.student;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecentScoreDTO {
    private String className;
    private String subjectName;
    private LocalDateTime submittedAt;
    private BigDecimal score;
    private String type; // "ASSIGNMENT" hoặc "QUIZ"
    private String title; // tên bài tập hoặc quiz

    public RecentScoreDTO(String className, String subjectName, LocalDateTime submittedAt,
                          BigDecimal score, String type, String title) {
        this.className = className;
        this.subjectName = subjectName;
        this.submittedAt = submittedAt;
        this.score = score;
        this.type = type;
        this.title = title;
    }

    // Getters and Setters
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
