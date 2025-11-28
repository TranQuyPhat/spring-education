package com.example.springboot_education.dtos.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserScheduleDTO {
    private Integer weekNumber;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private LocalDate previousWeekStartDate; // Để FE biết click "Previous" thì truyền date nào
    private LocalDate nextWeekStartDate; // Để FE biết click "Next" thì truyền date nào
    private List<DayScheduleDTO> schedules;
}
