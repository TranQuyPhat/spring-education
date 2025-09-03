package com.example.springboot_education.dtos.classschedules;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClassScheduleSessionCreateDTO {

    @NotNull(message = "PatternId is required")
    private Integer patternId;

    @NotNull(message = "ClassId is required")
    private Integer classId;

    @NotNull(message = "Session date is required")
    @FutureOrPresent(message = "Session date cannot be in the past")
    private LocalDate sessionDate;

    @NotNull(message = "Start period is required")
    @Min(value = 1, message = "Start period must be at least 1")
    private Integer startPeriod; // 1, 2, 3...

    @NotNull(message = "End period is required")
    @Min(value = 1, message = "End period must be at least 1")
    private Integer endPeriod;   // 1, 2, 3...

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Status is required")
    private String status; // ví dụ: PLANNED, COMPLETED...

    private String note;
}
