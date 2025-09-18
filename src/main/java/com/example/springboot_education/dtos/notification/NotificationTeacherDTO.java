package com.example.springboot_education.dtos.notification;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class NotificationTeacherDTO {
    private String studentName;
    private Integer classId;
    private String message;
}
