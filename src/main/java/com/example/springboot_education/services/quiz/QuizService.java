package com.example.springboot_education.services.quiz;


import com.example.springboot_education.dtos.quiz.*;

import java.util.List;

public interface QuizService {
    QuizResponseDTO createQuiz(QuizRequestDTO quizDTO);
    QuizResponseDTO getQuizById(Integer id);
    List<QuizResponseDTO> getAllQuizzes();
    QuizSubmitResDTO submitQuiz(QuizSubmitReqDTO request);
     List<QuizReportDTO> getReportByQuizId(Integer quizId);
}
