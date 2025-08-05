package com.example.springboot_education.dtos.submissionDTOs;

import com.example.springboot_education.entities.Submission;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class SubmissionResponseDto {
    private Integer id;
    private Integer assignmentId;
    private Integer studentId;
    private String filePath;
    private String fileType;
    private Submission.SubmissionStatus status;
    private BigDecimal score;
    private String teacherComment;
    private Timestamp submittedAt;
    private Timestamp gradedAt;
}
