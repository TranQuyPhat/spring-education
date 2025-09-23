package com.example.springboot_education.dtos.assignmentDTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Schema(description = "DTO for assignment response")
@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AssignmentResponseDto {

    @Schema(description = "Unique identifier of the assignment", example = "101")
    private Integer id;

    @Schema(description = "Title of the assignment", example = "Math Homework - Chapter 1")
    private String title;

    @Schema(description = "Detailed description of the assignment", example = "Solve all exercises from page 10 to 20")
    private String description;

    @Schema(description = "Identifier of the class this assignment belongs to", example = "5")
    private Integer classId;

    @Schema(description = "Due date of the assignment", example = "2025-09-30T23:59:59")
    private LocalDateTime dueDate;

    @Schema(description = "Maximum score achievable for this assignment", example = "100.0")
    private BigDecimal maxScore;

    @Schema(description = "Path of the attached file", example = "/uploads/assignments/math_chapter1.pdf")
    private String filePath;

    @Schema(description = "File type of the attached file", example = "application/pdf")
    private String fileType;

    @Schema(description = "Size of the attached file", example = "1.2 MB")
    private String fileSize;

    @Schema(description = "Name of the attached file", example = "math_chapter1.pdf")
    private String fileName;

    @Schema(description = "Whether the assignment has been published to students", example = "true")
    private boolean published;

    @Schema(description = "Timestamp when the assignment was created", example = "2025-09-01T10:15:30")
    private Timestamp createdAt;

    @Schema(description = "Timestamp when the assignment was last updated", example = "2025-09-15T14:45:00")
    private Timestamp updatedAt;
}
