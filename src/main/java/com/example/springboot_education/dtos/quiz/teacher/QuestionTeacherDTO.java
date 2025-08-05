package com.example.springboot_education.dtos.quiz.teacher;

import com.example.springboot_education.dtos.quiz.QuestionBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionTeacherDTO extends QuestionBaseDTO {
    private String correctOption; // Chỉ giáo viên thấy
    private BigDecimal score; // Có thể chỉ giáo viên thấy
}