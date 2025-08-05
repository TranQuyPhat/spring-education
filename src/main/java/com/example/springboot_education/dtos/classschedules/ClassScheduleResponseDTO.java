package com.example.springboot_education.dtos.classschedules;

import java.time.Instant;
import java.time.LocalTime;

import lombok.Data;

@Data
public class ClassScheduleResponseDTO {
    private Integer id;
    private Integer classId;
    private Integer dayOfWeek;
    private LocalTime  startTime;
    private LocalTime  endTime;
    private String location;
    private Instant createdAt;
    private Instant updatedAt;

    private String className;

}

