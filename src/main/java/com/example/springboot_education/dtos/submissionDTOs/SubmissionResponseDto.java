package com.example.springboot_education.dtos.submissionDTOs;

import com.example.springboot_education.entities.Submission;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class SubmissionResponseDto {
    private Integer id;
    private Integer assignmentId;
    private String filePath;
    private String fileType;
    private String fileSize;
    private String fileName;
    private String description;
    private Submission.SubmissionStatus status;
    private BigDecimal score;
    private String teacherComment;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;

    // 👇 Nested DTO cho student
    private StudentDto student;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentDto {
        private Integer id;
        private String fullName;
        private String email;
        private String avatarBase64;
    }

    private AssignmentDto assignment;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignmentDto {
        private Integer id;
        private String title;
        private boolean published;
    }
}
