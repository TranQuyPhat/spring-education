package com.example.springboot_education.services.quiz;


import com.example.springboot_education.dtos.quiz.QuestionsPageResponseDTO;
import com.example.springboot_education.dtos.quiz.QuizContentUpdateDTO;
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
    QuizResponseTeacherDTO updateQuizMeta(Integer quizId, QuizBaseDTO dto);
    QuizResponseTeacherDTO updateQuizContent(Integer quizId, QuizContentUpdateDTO body);
    void deleteQuestion(Integer quizId, Integer questionId);
    void deleteQuiz(Integer quizId);
    QuestionsPageResponseDTO getQuizQuestionsPageForTeacher(Integer quizId, int page, int size);
    QuestionsPageResponseDTO getQuizQuestionsPageForStudent(Integer quizId, int page, int size);
    List<QuizResponseStudentDTO> getQuizzesByStudentId(Integer studentId);
List<QuizResponseTeacherDTO> getQuizzesByTeacherId(Integer teacherId);}



