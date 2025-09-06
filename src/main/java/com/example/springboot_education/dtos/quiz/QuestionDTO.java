package com.example.springboot_education.dtos.quiz;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class QuestionDTO {
    private int id;
    @NotBlank(message = "Question text is required")
    private String questionText;
    @NotBlank(message = "Question type is required")
    @Pattern(regexp = "ONE_CHOICE|MULTI_CHOICE|TRUE_FALSE|FILL_BLANK", message = "Invalid question type")
    private String questionType; // MULTIPLE_CHOICE, TRUE_FALSE, etc.
    private String correctOption;
    @NotNull(message = "Score is required")
    @DecimalMin(value = "0", inclusive = true, message = "Score must be at least 0")
    @DecimalMax(value = "10.0", inclusive = true, message = "Score cannot be greater than 10")
    private BigDecimal score;
    @NotEmpty(message = "Options cannot be empty")
    private List<OptionDTO> options;
}
