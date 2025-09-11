package com.example.springboot_education.dtos.assignmentDTOs;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssignmentRequestDto {

   
    @NotNull(message = "Class ID is required")
    private Integer classId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;

    @Positive(message = "Max score must be greater than 0")
    private BigDecimal maxScore;

    // Optional file info
    private String filePath;
    private String fileType;

    @Positive(message = "File size must be positive")
    private Long fileSize;
}

