package com.example.springboot_education.services.quiz;


import com.example.springboot_education.dtos.quiz.QuizBaseDTO;
import com.example.springboot_education.dtos.quiz.QuizRequestDTO;
import com.example.springboot_education.dtos.quiz.QuizSubmitReqDTO;
import com.example.springboot_education.dtos.quiz.QuizSubmitResDTO;
import com.example.springboot_education.dtos.quiz.student.QuizResponseStudentDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuizResponseTeacherDTO;

import java.util.List;

public interface QuizService {
    QuizBaseDTO createQuiz(QuizRequestDTO quizDTO);
    QuizBaseDTO getQuizById(Integer id);
    List<QuizBaseDTO> getAllQuizzes();
    QuizSubmitResDTO submitQuiz(QuizSubmitReqDTO request);
    QuizResponseTeacherDTO getQuizForTeacher(Integer quizId);
    QuizResponseStudentDTO getQuizForStudent(Integer quizId);

}
