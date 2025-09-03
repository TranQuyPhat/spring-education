package com.example.springboot_education.dtos.classDTOs;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddStudentToClassDTO {
    @NotNull(message = "Class ID is required")
    @Min(value = 1, message = "Class ID must be greater than 0")
    private Integer classId;

    @NotNull(message = "Student ID is required")
    @Min(value = 1, message = "Student ID must be greater than 0")
    private Integer studentId;
}
