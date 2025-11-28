package com.example.springboot_education.dtos.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayScheduleDTO {
    private String day; // "Monday", "Tuesday", etc.
    private LocalDate date;
    private List<ScheduleSessionDTO> lessons;
}
