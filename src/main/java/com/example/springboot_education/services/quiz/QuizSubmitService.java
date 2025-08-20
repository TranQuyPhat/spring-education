package com.example.springboot_education.services.quiz;

import com.example.springboot_education.dtos.quiz.submit.QuizSubmissionBaseDTO;
import com.example.springboot_education.dtos.quiz.submit.QuizSubmitReqDTO;

import java.util.List;

public interface QuizSubmitService {
    List<QuizSubmissionBaseDTO> getSubmissionsByQuizId(Integer quizId);
    QuizSubmissionBaseDTO submitQuiz(QuizSubmitReqDTO request);

}
