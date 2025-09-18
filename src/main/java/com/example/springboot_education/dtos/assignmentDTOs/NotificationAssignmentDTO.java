package com.example.springboot_education.dtos.assignmentDTOs;

import lombok.*;

import java.time.Instant;



@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class NotificationAssignmentDTO {
    private String title;
    private String description;
    private Integer classId;
    private Instant dueDate;
    private String message;
}
