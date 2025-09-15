package com.example.springboot_education.mapper;

import com.example.springboot_education.dtos.quiz.OptionDTO;
import com.example.springboot_education.dtos.quiz.QuizRequestDTO;
import com.example.springboot_education.dtos.quiz.base.QuizBaseDTO;
import com.example.springboot_education.dtos.quiz.student.QuestionStudentDTO;
import com.example.springboot_education.dtos.quiz.student.QuizResponseStudentDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuestionTeacherDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuizResponseTeacherDTO;
import com.example.springboot_education.entities.*;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.classes.ClassUserRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QuizMapper2 {
    private final UsersJpaRepository userRepository;
    private final ClassRepository classRepository;
    private final ClassUserRepository classUserRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public void mapBaseFields(Quiz quiz, QuizBaseDTO dto) {
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setTimeLimit(quiz.getTimeLimit());
        dto.setStartDate(quiz.getStartDate() != null ?
                quiz.getStartDate().atZone(VIETNAM_ZONE).toOffsetDateTime() : null);

        dto.setEndDate(quiz.getEndDate() != null ?
                quiz.getEndDate().atZone(VIETNAM_ZONE).toOffsetDateTime() : null);

        dto.setSubject(quiz.getSubject());
    }
    public QuizResponseTeacherDTO toTeacherDto(Quiz quiz, List<QuestionTeacherDTO> questions) {
        if (quiz == null) return null;
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
        dto.setQuestionType(question.getQuestionType());
        dto.setCorrectOptions(question.getCorrectOptions()); // hoặc rename thành correctOptions
        dto.setScore(question.getScore());
        dto.setOptions(options);

        // Bổ sung phần FILL_BLANK và regex
        dto.setCorrectAnswerTexts(question.getCorrectAnswerTexts());
        dto.setCorrectAnswerRegex(question.getCorrectAnswerRegex());
        dto.setCaseSensitive(question.isCaseSensitive());
        dto.setTrimWhitespace(question.isTrimWhitespace());

        return dto;
    }


    public QuestionStudentDTO toStudentQuestionDto(QuizQuestion question, List<OptionDTO> options) {
        QuestionStudentDTO dto = new QuestionStudentDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setOptions(options);
        dto.setScore(question.getScore());
        return dto;
    }

    public OptionDTO toOptionDto(QuizOption opt) {
        OptionDTO dto = new OptionDTO();
        dto.setId(opt.getId());
        dto.setOptionLabel(opt.getOptionLabel());
        dto.setOptionText(opt.getOptionText());
        return dto;
    }


    public Quiz toEntity(QuizRequestDTO dto) {
        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setTimeLimit(dto.getTimeLimit());

        // OffsetDateTime → Instant
        quiz.setStartDate(dto.getStartDate() != null ? dto.getStartDate().toInstant() : null);
        quiz.setEndDate(dto.getEndDate() != null ? dto.getEndDate().toInstant() : null);

        // Lấy class
        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class with ID: " + dto.getClassId()));
        quiz.setClassField(classEntity);

        // Lấy user tạo
        Users creator = userRepository.findById(dto.getCreatedBy())
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + dto.getCreatedBy()));
        quiz.setCreatedBy(creator);

        // createdAt, updatedAt = now (UTC)
        Instant now = Instant.now();
        quiz.setCreatedAt(now);
        quiz.setUpdatedAt(now);

        return quiz;
    }



}
