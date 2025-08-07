package com.example.springboot_education.mapper;

import com.example.springboot_education.dtos.quiz.OptionDTO;
import com.example.springboot_education.dtos.quiz.base.QuizBaseDTO;
import com.example.springboot_education.dtos.quiz.QuizRequestDTO;
import com.example.springboot_education.dtos.quiz.student.QuestionStudentDTO;
import com.example.springboot_education.dtos.quiz.student.QuizResponseStudentDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuestionTeacherDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuizResponseTeacherDTO;
import com.example.springboot_education.entities.*;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.ClassUserRepository;
import com.example.springboot_education.repositories.UserRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QuizMapper2 {
    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final ClassUserRepository classUserRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private void mapBaseFields(Quiz quiz, QuizBaseDTO dto) {
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setTimeLimit(quiz.getTimeLimit());
        dto.setStartDate(quiz.getStartDate());
        dto.setEndDate(quiz.getEndDate());
        dto.setGrade(quiz.getGrade());
        dto.setSubject(quiz.getSubject());
    }
    public QuizResponseTeacherDTO toTeacherDto(Quiz quiz, List<QuestionTeacherDTO> questions) {
        QuizResponseTeacherDTO dto = new QuizResponseTeacherDTO();
        mapBaseFields(quiz, dto);
        dto.setClassId(quiz.getClassField().getId());
        dto.setCreatedBy(quiz.getCreatedBy().getId());
        dto.setClassName(quiz.getClassField().getClassName());
        dto.setTotalStudents(classUserRepository.countByClassField_Id(quiz.getClassField().getId()));
        dto.setStudentsSubmitted(quizSubmissionRepository.countByQuiz_Id(quiz.getId()));
        dto.setQuestions(questions);
        return dto;
    }
    public QuizResponseStudentDTO toStudentDto(Quiz quiz, List<QuestionStudentDTO> questions) {
        QuizResponseStudentDTO dto = new QuizResponseStudentDTO();
        mapBaseFields(quiz, dto);
        dto.setQuestions(questions);
        return dto;
    }

    public QuestionTeacherDTO toTeacherQuestionDto(QuizQuestion question, List<OptionDTO> options) {
        QuestionTeacherDTO dto = new QuestionTeacherDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setCorrectOption(question.getCorrectOption().toString());
        dto.setScore(question.getScore());
        dto.setOptions(options);
        return dto;
    }

    public QuestionStudentDTO toStudentQuestionDto(QuizQuestion question, List<OptionDTO> options) {
        QuestionStudentDTO dto = new QuestionStudentDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setOptions(options);
        dto.setScore(question.getScore());
        return dto;
    }

    public OptionDTO toOptionDto(QuizOption opt) {
        OptionDTO dto = new OptionDTO();
        dto.setOptionLabel(opt.getOptionLabel());
        dto.setOptionText(opt.getOptionText());
        return dto;
    }

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



}
