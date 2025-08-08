package com.example.springboot_education.dtos.quiz.teacher;

import com.example.springboot_education.dtos.quiz.base.QuestionBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionTeacherDTO extends QuestionBaseDTO {
    private String correctOption;

}