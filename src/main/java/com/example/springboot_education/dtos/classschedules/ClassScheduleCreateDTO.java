package com.example.springboot_education.dtos.classschedules;

import java.time.LocalTime;

import lombok.Data;

@Data
public class ClassScheduleCreateDTO {
    private Integer classId;
    private Integer dayOfWeek;
    private LocalTime  startTime;
    private LocalTime  endTime;
    private String location;
}
