package com.example.springboot_education.dtos.dashboard.student;

import java.math.BigDecimal;
import java.time.LocalDateTime;
public class ClassResultDTO {
    private String className;
    private String subjectName;
    private String assignmentTitle;
    private String type; // "ASSIGNMENT" hoặc "QUIZ"
    private LocalDateTime submittedAt;
    private BigDecimal score;

    public ClassResultDTO(String className, String subjectName, String assignmentTitle,
                          String type, LocalDateTime submittedAt, BigDecimal score) {
        this.className = className;
        this.subjectName = subjectName;
        this.assignmentTitle = assignmentTitle;
        this.type = type;
        this.submittedAt = submittedAt;
        this.score = score;
    }

    // Getters and Setters
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
}
