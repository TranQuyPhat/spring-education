package com.example.springboot_education.dtos.submissionDTOs;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor

public class SubmissionRequestDto {
    private Integer assignmentId;
    private Integer studentId;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String description;
}
