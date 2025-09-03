package com.example.springboot_education.dtos.attendances;


import com.example.springboot_education.entities.Attendance.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceRequestDTO {

    @NotNull
    private Integer studentId;

    // @NotNull
    // private Integer sessionId;

    @NotNull
    private AttendanceStatus status;

    private String note;
}