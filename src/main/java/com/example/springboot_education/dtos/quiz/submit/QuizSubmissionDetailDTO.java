package com.example.springboot_education.dtos.quiz.submit;

import com.example.springboot_education.dtos.quiz.QuestionAnswerDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class QuizSubmissionDetailDTO {
    private List<QuestionAnswerDTO> questions;
}
