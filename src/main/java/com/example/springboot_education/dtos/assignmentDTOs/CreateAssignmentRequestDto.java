package com.example.springboot_education.dtos.assignmentDTOs;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotNull(message = "Due date is required")
    private Instant dueDate;

    @NotNull(message = "Max score is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Max score must be positive")
    private BigDecimal maxScore;
}
