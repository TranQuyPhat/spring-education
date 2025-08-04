package com.example.springboot_education.mapper;

import com.example.springboot_education.dtos.quiz.OptionDTO;
import com.example.springboot_education.dtos.quiz.QuestionResponseDTO;
import com.example.springboot_education.dtos.quiz.QuizRequestDTO;
import com.example.springboot_education.dtos.quiz.QuizResponseDTO;
import com.example.springboot_education.entities.*;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.ClassUserRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import com.example.springboot_education.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
@RequiredArgsConstructor
@Component
public class QuizMapper {
    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final ClassUserRepository classUserRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    public Quiz toEntity(QuizRequestDTO dto) {
        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setTimeLimit(dto.getTimeLimit());
        quiz.setStartDate(dto.getStartDate());
        quiz.setEndDate(dto.getEndDate());
        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found with ID: " + dto.getClassId()));
        quiz.setClassField(classEntity);
        Users creator = userRepository.findById(dto.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getCreatedBy()));
        quiz.setCreatedBy(creator);

        quiz.setGrade(dto.getGrade());
        quiz.setSubject(dto.getSubject());
        quiz.setCreatedAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        quiz.setUpdatedAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        return quiz;
    }

    public QuizResponseDTO toDto(Quiz quiz, List<QuestionResponseDTO> questions) {
        QuizResponseDTO dto = new QuizResponseDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setTimeLimit(quiz.getTimeLimit());
        dto.setStartDate(quiz.getStartDate());
        dto.setEndDate(quiz.getEndDate());
        dto.setCreatedBy(quiz.getCreatedBy().getId());
        dto.setGrade(quiz.getGrade());
        dto.setSubject(quiz.getSubject());
        dto.setQuestions(questions);
        dto.setClassName(quiz.getClassField().getClassName());
        if (quiz.getClassField() != null) {
            dto.setClassId(quiz.getClassField().getId());
            dto.setClassName(quiz.getClassField().getClassName());
            int totalStudents = classUserRepository.countByClassField_Id(quiz.getClassField().getId());
            dto.setTotalStudents(totalStudents);
        } else {
            dto.setClassName(null);
            dto.setTotalStudents(0);
        }
        int submittedCount = quizSubmissionRepository.countByQuiz_Id(quiz.getId());
        dto.setStudentsSubmitted(submittedCount);
        return dto;
    }

    public QuestionResponseDTO toDto(QuizQuestion q, List<OptionDTO> options) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setId(q.getId());
        dto.setQuestionText(q.getQuestionText());
        dto.setCorrectOption(q.getCorrectOption().toString());
        dto.setScore(q.getScore());
        dto.setOptions(options);
        return dto;
    }

    public OptionDTO toDto(QuizOption opt) {
        OptionDTO dto = new OptionDTO();
        dto.setOptionLabel(opt.getOptionLabel());
        dto.setOptionText(opt.getOptionText());
        return dto;
    }
}
