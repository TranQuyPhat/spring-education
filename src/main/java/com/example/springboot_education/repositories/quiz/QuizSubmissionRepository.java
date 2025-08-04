package com.example.springboot_education.repositories.quiz;

import com.example.springboot_education.entities.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    int countByQuiz_Id(Integer quizId);
}
