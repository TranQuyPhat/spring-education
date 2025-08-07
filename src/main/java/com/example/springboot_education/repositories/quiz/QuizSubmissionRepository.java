package com.example.springboot_education.repositories.quiz;

import com.example.springboot_education.entities.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Integer> {
    int countByQuiz_Id(Integer quizId);
    @Query("SELECT qs FROM QuizSubmission qs " +
            "JOIN FETCH qs.student s " +
            "JOIN FETCH qs.quiz q " +
            "LEFT JOIN FETCH q.classField " +
            "WHERE q.id = :quizId")
    List<QuizSubmission> findAllByQuizIdWithDetails(@Param("quizId") Integer quizId);

}
