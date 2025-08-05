package com.example.springboot_education.dtos.quiz.student;

import com.example.springboot_education.dtos.quiz.QuizBaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizResponseStudentDTO extends QuizBaseDTO {

    private List<QuestionStudentDTO> questions;
}