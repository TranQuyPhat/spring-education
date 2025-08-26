package com.example.springboot_education.dtos.assignmentDTOs;

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
public class NotificationAssignmentDTO {
    private String title;
    private String description;
    private Integer classId;
    private LocalDateTime dueDate;
}
