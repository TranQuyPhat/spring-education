package com.example.springboot_education.dtos.submissionDTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor

public class SubmissionRequestDto {
    @NotNull(message = "Assignment ID cannot be null")
    private Integer assignmentId;

    @NotNull(message = "Student ID cannot be null")
    private Integer studentId;

    private String filePath;

    private String fileType;

    private Long fileSize;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
