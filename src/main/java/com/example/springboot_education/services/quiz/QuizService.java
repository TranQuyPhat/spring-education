package com.example.springboot_education.services.quiz;


import com.example.springboot_education.dtos.quiz.QuizRequestDTO;
import com.example.springboot_education.dtos.quiz.base.QuizBaseDTO;
import com.example.springboot_education.dtos.quiz.student.QuizResponseStudentDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuizResponseTeacherDTO;

import java.util.List;

public interface QuizService {
    QuizBaseDTO createQuiz(QuizRequestDTO quizDTO);
    List<QuizResponseTeacherDTO> getAllQuizzes();
    QuizResponseTeacherDTO getQuizForTeacher(Integer quizId);
    QuizResponseStudentDTO getQuizForStudent(Integer quizId);


}
