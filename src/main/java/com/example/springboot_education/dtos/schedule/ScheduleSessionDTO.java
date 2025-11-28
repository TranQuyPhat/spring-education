package com.example.springboot_education.dtos.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSessionDTO {
    private Integer sessionId;
    private String className;
    private String subjectName;
    private String teacherName;
    private Integer startPeriod;
    private Integer endPeriod;
    private String location;
    private String sessionStatus; // SCHEDULED, COMPLETED, CANCELLED
    private LocalDate sessionDate;
}
