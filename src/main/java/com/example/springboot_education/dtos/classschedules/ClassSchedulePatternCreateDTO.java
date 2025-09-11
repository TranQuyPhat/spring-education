package com.example.springboot_education.dtos.classschedules;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClassSchedulePatternCreateDTO {

    @NotNull(message = "Class ID is required")
    private Integer classId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotEmpty(message = "At least one slot is required")
    @Valid
    private List<SlotDTO> slots;

    @Data
    public static class SlotDTO {
        @NotBlank(message = "Day of week is required")
        private String dayOfWeek;   // MONDAY, TUESDAY...

        @NotNull(message = "Start period is required")
        @Min(value = 1, message = "Start period must be >= 1")
        private Integer startPeriod;

        @NotNull(message = "End period is required")
        @Min(value = 1, message = "End period must be >= 1")
        private Integer endPeriod;

        @NotNull(message = "Location ID is required")
        private Integer locationId; // room id
    }
}
