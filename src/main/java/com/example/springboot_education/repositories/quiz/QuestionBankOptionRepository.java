package com.example.springboot_education.repositories.quiz;

import com.example.springboot_education.entities.nQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionBankOptionRepository extends JpaRepository<nQuestionOption, Long> {
    List<nQuestionOption> findByQuestion_Id(Long questionId);
}
