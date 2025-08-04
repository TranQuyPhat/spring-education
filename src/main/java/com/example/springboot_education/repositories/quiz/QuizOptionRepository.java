package com.example.springboot_education.repositories.quiz;

import com.example.springboot_education.entities.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface QuizOptionRepository extends JpaRepository<QuizOption, Integer> {
    List<QuizOption> findByQuestion_Id(Integer questionId);
}
