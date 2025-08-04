package com.example.springboot_education.repositories.quiz;

import com.example.springboot_education.entities.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion,Integer> {
    List<QuizQuestion> findByQuiz_Id(Integer quizId);
}
