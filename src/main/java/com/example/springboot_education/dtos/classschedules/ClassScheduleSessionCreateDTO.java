package com.example.springboot_education.dtos.classschedules;


import java.time.LocalDate;

import lombok.Data;
@Data
public class ClassScheduleSessionCreateDTO {
    private Integer patternId;
    private Integer classId;
    private LocalDate sessionDate;
    private Integer startPeriod; // 1, 2, 3...
    private Integer endPeriod;   // 1, 2, 3...
    private String location;
    private String status;
    private String note;
}
