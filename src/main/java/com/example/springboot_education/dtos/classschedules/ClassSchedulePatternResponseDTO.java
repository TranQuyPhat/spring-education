package com.example.springboot_education.dtos.classschedules;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ClassSchedulePatternResponseDTO {
    private Integer id;
    private Integer classId;
    private String className;
    private String dayOfWeek; // trả string cho client
    private Integer startPeriod; // 1, 2, 3...
    private Integer endPeriod;   // 1, 2, 3...
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
}