package com.example.springboot_education.dtos.classschedules;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class RecurringScheduleRequest{
    private Integer classId;
    private DayOfWeek dayOfWeek;
    private LocalTime  startTime;
    private LocalTime  endTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
}
