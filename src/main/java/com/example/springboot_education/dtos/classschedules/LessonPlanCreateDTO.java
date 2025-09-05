package com.example.springboot_education.dtos.classschedules;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonPlanCreateDTO {
    @NotNull(message = "Session number is required")
    @Min(value = 1, message = "Session number must be greater than 0")
    private Integer sessionNumber;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
}
