package com.example.springboot_education.dtos.quiz;

import com.example.springboot_education.dtos.quiz.teacher.QuestionTeacherDTO;
import lombok.Data;

import java.util.List;

@Data
public class QuizContentUpdateDTO {
    private List<QuestionTeacherDTO> questions;
    private boolean replaceAll;
}
