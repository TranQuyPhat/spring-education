package com.example.springboot_education.repositories.quiz;

import com.example.springboot_education.entities.nQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionBankRepository extends JpaRepository<nQuestion, Long> {
}
