package com.example.springboot_education.dtos.classschedules;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClassScheduleSessionUpdateDTO {

    @NotNull(message = "Session date is required")
    @FutureOrPresent(message = "Session date must be today or in the future")
    private LocalDate sessionDate;

    @NotNull(message = "Start period is required")
    @Min(value = 1, message = "Start period must be >= 1")
    private Integer startPeriod;

    @NotNull(message = "End period is required")
    @Min(value = 1, message = "End period must be >= 1")
    private Integer endPeriod;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Status is required")
    private String status;

    private String note;
}
