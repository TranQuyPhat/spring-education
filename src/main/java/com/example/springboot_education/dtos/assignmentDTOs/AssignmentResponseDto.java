package com.example.springboot_education.dtos.assignmentDTOs;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;


@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AssignmentResponseDto {
    private Integer id;
    private String title;
    private String description;
    private Integer classId;
    private LocalDateTime dueDate;
    private BigDecimal maxScore;
    private String filePath;
    private String fileType;
    private String fileSize;
    private String fileName;
    private boolean published;
    private Timestamp createdAt;
    private Timestamp updatedAt;

}
