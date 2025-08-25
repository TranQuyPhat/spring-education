package com.example.springboot_education.dtos.classschedules;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClassSchedulePatternCreateDTO {
    private Integer classId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<SlotDTO> slots;

    @Data
    public static class SlotDTO {
        private String dayOfWeek;   // MONDAY, TUESDAY...
        private Integer startPeriod; // 1, 2, 3...
        private Integer endPeriod;   // 1, 2, 3...  
        private Integer locationId;    // roomName
    }
}