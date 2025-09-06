package com.example.springboot_education.dtos.classschedules;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassSchedulePatternUpdateDTO {

    @NotNull(message = "Patterns list cannot be null")
    @Valid
    private List<PatternUpdateDTO> patterns;

    @Data
    public static class PatternUpdateDTO {
        @NotNull(message = "Pattern ID is required")
        private Integer id;

        @NotBlank(message = "Day of week is required")
        private String dayOfWeek;   // MONDAY, TUESDAY...

        @NotNull(message = "Start period is required")
        @Min(value = 1, message = "Start period must be >= 1")
        private Integer startPeriod;

        @NotNull(message = "End period is required")
        @Min(value = 1, message = "End period must be >= 1")
        private Integer endPeriod;

        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        @NotNull(message = "Location ID is required")
        private Integer locationId;
    }
}
