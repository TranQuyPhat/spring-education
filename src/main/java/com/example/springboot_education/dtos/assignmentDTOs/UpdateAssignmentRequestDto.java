package com.example.springboot_education.dtos.assignmentDTOs;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssignmentRequestDto {

    private Integer classId;
    private String title;
    private String description;
    private Instant dueDate;
    private BigDecimal maxScore;
    private String filePath;
    private String fileType;
}

