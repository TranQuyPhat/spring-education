package com.example.springboot_education.repositories.quiz;

import com.example.springboot_education.entities.QuizQuestion;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion,Integer> {
    @Query("SELECT q FROM QuizQuestion q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE q.quiz.id = :quizId " +
            "ORDER BY q.id")
    List<QuizQuestion> findQuestionsWithOptionsByQuizId(@Param("quizId") Integer quizId);
    Page<QuizQuestion> findByQuiz_Id(Integer quizId, Pageable pageable);

    long countByQuiz_Id(Integer quizId);
}
