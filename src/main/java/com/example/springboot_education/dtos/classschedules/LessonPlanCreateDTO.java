package com.example.springboot_education.dtos.classschedules;
import lombok.Data;

@Data
public class LessonPlanCreateDTO {
    private Integer sessionNumber;
    private String title;
    private String description;
}
