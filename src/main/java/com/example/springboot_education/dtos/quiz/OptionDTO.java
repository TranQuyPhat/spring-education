package com.example.springboot_education.dtos.quiz;

import lombok.Data;

/**
 */
@Data
public class OptionDTO {
    private String optionLabel; // e.g., A, B, C, D
    private String optionText;
}