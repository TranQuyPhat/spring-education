package com.example.springboot_education.repositories.quiz;

import com.example.springboot_education.entities.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Integer> {
}
