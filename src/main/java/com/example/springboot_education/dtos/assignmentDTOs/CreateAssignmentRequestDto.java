package com.example.springboot_education.dtos.assignmentDTOs;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequestDto {

    @NotNull(message = "Title is required")
    private String title;
    private String description;

    @NotNull(message = "Class ID is required")
    private Integer classId;
    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    @NotNull(message = "Max score is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Max score must be positive")
    private BigDecimal maxScore;
    private String filePath;
    private String fileType;
    private Long fileSize;
}
