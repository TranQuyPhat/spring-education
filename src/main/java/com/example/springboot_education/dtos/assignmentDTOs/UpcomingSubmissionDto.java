package com.example.springboot_education.dtos.assignmentDTOs;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingSubmissionDto {
    private Integer id;
    private String title;
    private String className;
    private LocalDateTime dueDate;
    private Integer daysLeft;
    private Integer submittedCount; 
    private Integer totalStudents;  
}