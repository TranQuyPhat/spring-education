package com.example.springboot_education.dtos.assignmentDTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "DTO for creating a new assignment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequestDto {

    @Schema(description = "Title of the assignment", example = "Math Homework - Chapter 1")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Detailed description of the assignment", example = "Solve exercises from page 10 to 20")
    private String description;

    @Schema(description = "Identifier of the class this assignment belongs to", example = "5")
    @NotNull(message = "Class ID is required")
    private Integer classId;

    @Schema(description = "Due date of the assignment", example = "2025-09-30T23:59:59")
    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    @Schema(description = "Maximum score achievable for this assignment", example = "100.0")
    @NotNull(message = "Max score is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Max score must be positive")
    private BigDecimal maxScore;

    @Schema(description = "Path of the attached file", example = "/uploads/assignments/math_chapter1.pdf")
    private String filePath;

    @Schema(description = "File type of the attached file", example = "application/pdf")
    private String fileType;

    @Schema(description = "Size of the attached file in bytes", example = "1256000")
    private Long fileSize;
}
