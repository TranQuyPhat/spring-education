package com.example.springboot_education.dtos.attendances;

import java.time.Instant;

import lombok.Data;

@Data
public class AttendanceResponseDTO {
    private Integer id;
    private Integer studentId;
    private Integer scheduleId;
    private Instant markedAt;
    private String status;
    private String fullName;
    private String className;
}
