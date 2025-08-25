package com.example.springboot_education.dtos.classschedules;


import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class ClassSchedulePatternUpdateDTO {
    private List<PatternUpdateDTO> patterns;

    @Data
    public static class PatternUpdateDTO {
        private Integer id;
        private String dayOfWeek;
        private Integer startPeriod;
        private Integer endPeriod;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer locationId;
    }
}