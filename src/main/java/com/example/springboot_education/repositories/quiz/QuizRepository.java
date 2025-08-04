package com.example.springboot_education.repositories.quiz;


import com.example.springboot_education.entities.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    @EntityGraph(attributePaths = {"questions"})
    Optional<Quiz> findById(Long id);

}