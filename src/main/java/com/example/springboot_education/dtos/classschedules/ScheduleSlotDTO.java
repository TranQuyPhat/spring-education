package com.example.springboot_education.dtos.classschedules;

import java.time.DayOfWeek;
import java.time.LocalTime;

import lombok.Data;

@Data
public class ScheduleSlotDTO {
    private DayOfWeek dayOfWeek;   // VD: MONDAY
    private LocalTime startTime;   // VD: 14:00
    private LocalTime endTime;     // VD: 16:00
    private Integer locationId;       // VD: "Phòng 302"
}
