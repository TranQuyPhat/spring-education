package com.example.springboot_education.dtos.classschedules;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonPlanDTO {
    private Integer id;
    private Integer sessionNumber;
    private String title;
    private String description;
}