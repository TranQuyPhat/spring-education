package com.example.springboot_education.dtos.classschedules;

import java.time.LocalTime;

import lombok.Data;

@Data
public class ClassScheduleUpdateDTO {
    private Integer dayOfWeek;
    private LocalTime  startTime;
    private LocalTime  endTime;
    private String location;
}
