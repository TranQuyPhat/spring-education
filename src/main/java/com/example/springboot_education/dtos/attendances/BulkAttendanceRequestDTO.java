package com.example.springboot_education.dtos.attendances;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkAttendanceRequestDTO {

    @NotNull
    private Integer sessionId;

    @NotNull
    private List<AttendanceRequestDTO> records;
}