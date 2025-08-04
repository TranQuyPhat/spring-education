package com.example.springboot_education.dtos.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponseDTO {
    private Integer id;
    private String questionText;
    private String questionType;
    private String correctOption;
    private BigDecimal score;
    private List<OptionDTO> options;
}
