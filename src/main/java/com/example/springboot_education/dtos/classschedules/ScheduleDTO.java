package com.example.springboot_education.dtos.classschedules;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDTO {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer classId;

    private Integer sessionNumber;   // Buổi số mấy
    private String lessonTitle;      // Ví dụ "Chương 1: Giới thiệu"
    private String lessonDescription;
}
