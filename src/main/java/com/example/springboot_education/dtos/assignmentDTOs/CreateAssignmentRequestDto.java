package com.example.springboot_education.dtos.assignmentDTOs;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequestDto {

    private String title;
    private String description;

    @NotNull(message = "Class ID is required")

    @NotNull(message = "Due date is required")
    private Date dueDate;

    @NotNull(message = "Max score is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Max score must be positive")
    private BigDecimal maxScore;
    private String filePath;
    private String fileType;
}
