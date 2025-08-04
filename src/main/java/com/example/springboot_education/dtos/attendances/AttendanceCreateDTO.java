package com.example.springboot_education.dtos.attendances;

import lombok.Data;

@Data
public class AttendanceCreateDTO {
    private Integer scheduleId;
    private Integer studentId;
    private String status;  // e.g., "Present", "Absent", "Late"
}
