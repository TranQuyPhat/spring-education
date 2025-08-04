package com.example.springboot_education.dtos.submissionDTOs;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeSubmissionRequestDto {

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", message = "Score must be >= 0")
    @DecimalMax(value = "10.0", message = "Score must be <= 10")
    private BigDecimal score;

    @Size(max = 1000, message = "Comment must be <= 1000 characters")
    private String comment;
}