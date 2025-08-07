package com.example.springboot_education.dtos.quiz.base;

import com.example.springboot_education.dtos.quiz.OptionDTO;
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
public class QuestionBaseDTO {
    private Integer id;
    private String questionText;
    private String questionType;
    private List<OptionDTO> options;
    private BigDecimal score;
}