package com.example.springboot_education.mapper;

import com.example.springboot_education.dtos.quiz.OptionDTO;
import com.example.springboot_education.dtos.quiz.QuizBaseDTO;
import com.example.springboot_education.dtos.quiz.student.QuestionStudentDTO;
import com.example.springboot_education.dtos.quiz.student.QuizResponseStudentDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuestionTeacherDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuizResponseTeacherDTO;
import com.example.springboot_education.entities.Quiz;
import com.example.springboot_education.entities.QuizOption;
import com.example.springboot_education.entities.QuizQuestion;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.ClassUserRepository;
import com.example.springboot_education.repositories.UserRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuizMapper2 {
    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final ClassUserRepository classUserRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    // Map chung các trường cơ bản
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

    // DTO cho giáo viên
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

    // DTO cho học sinh
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

    // Map question cho học sinh (không có đáp án)
    public QuestionStudentDTO toStudentQuestionDto(QuizQuestion question, List<OptionDTO> options) {
        QuestionStudentDTO dto = new QuestionStudentDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setOptions(options);
        return dto;
    }

    // Giữ nguyên map OptionDTO
    public OptionDTO toOptionDto(QuizOption opt) {
        OptionDTO dto = new OptionDTO();
        dto.setOptionLabel(opt.getOptionLabel());
        dto.setOptionText(opt.getOptionText());
        return dto;
    }
}
