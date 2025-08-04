package com.example.springboot_education.dtos.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionRequest {
    private String questionText;
    private Character correctOption;
    private Map<String, String> options;
    private BigDecimal score;
}
