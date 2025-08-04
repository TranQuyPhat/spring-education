package com.example.springboot_education.dtos.assignmentDTOs;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class AssignmentResponseDto {
    private Integer id;
    private String title;
    private String description;
    private Integer classId;
    private Instant dueDate;
    private BigDecimal maxScore;
    private Instant createdAt;
    private Instant updatedAt;
    private String filePath;
    private String fileType;

    public AssignmentResponseDto(Integer id, String title, String description, Integer classId, Instant dueDate, BigDecimal maxScore, Instant createdAt, Instant updatedAt, String filePath, String fileType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.classId = classId;
        this.dueDate = dueDate;
        this.maxScore = maxScore;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.filePath = filePath;
        this.fileType = fileType;
    }
}
