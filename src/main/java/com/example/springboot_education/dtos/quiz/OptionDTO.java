package com.example.springboot_education.dtos.quiz;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 */
@Data
public class OptionDTO {
    private Integer id;
    @NotBlank(message = "Option label is required")
    private String optionLabel; // e.g., A, B, C, D
    @NotBlank(message = "Option text is required")
    private String optionText;
}