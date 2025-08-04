package com.example.springboot_education.dtos.quiz;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class QuestionDTO {
    private String questionText;
    private String questionType; // MULTIPLE_CHOICE, TRUE_FALSE, etc.
    private String correctOption;
    private BigDecimal score;
    private List<OptionDTO> options;
}
