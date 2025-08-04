package com.example.springboot_education.services.quiz.impl;

import com.example.springboot_education.dtos.quiz.*;
import com.example.springboot_education.entities.*;
import com.example.springboot_education.mapper.QuizMapper;
import com.example.springboot_education.repositories.UserRepository;
import com.example.springboot_education.repositories.quiz.*;
import com.example.springboot_education.services.quiz.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final QuizReportRepository quizReportRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final QuizMapper quizMapper;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserRepository userRepository;

    @Override
    public QuizResponseDTO createQuiz(QuizRequestDTO quizDTO) {
        Quiz quiz = quizMapper.toEntity(quizDTO);
        quiz = quizRepository.save(quiz);

        for (QuestionDTO qdto : quizDTO.getQuestions()) {
            QuizQuestion question = new QuizQuestion();
            question.setQuiz(quiz);
            question.setQuestionText(qdto.getQuestionText());
            question.setCorrectOption(qdto.getCorrectOption().charAt(0));
            question.setScore(qdto.getScore());
            question.setCreatedAt(Instant.now());
            question.setUpdatedAt(Instant.now());
            question = questionRepository.save(question);

            for (OptionDTO opt : qdto.getOptions()) {
                QuizOption option = new QuizOption();
                option.setQuestion(question);
                option.setOptionLabel(opt.getOptionLabel());
                option.setOptionText(opt.getOptionText());
                option.setCreatedAt(Instant.now());
                option.setUpdatedAt(Instant.now());
                optionRepository.save(option);
            }
        }

        return getQuizById(quiz.getId());
    }

    @Override
    public QuizResponseDTO getQuizById(Integer id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        List<QuizQuestion> questions = questionRepository.findByQuiz_Id(quiz.getId());

        List<QuestionResponseDTO> questionDTOs = questions.stream().map(q -> {
            List<QuizOption> options = optionRepository.findByQuestion_Id(q.getId());
            List<OptionDTO> optionDTOs = options.stream().map(quizMapper::toDto).toList();
            return quizMapper.toDto(q, optionDTOs);
        }).toList();

        return quizMapper.toDto(quiz, questionDTOs);
    }

    @Override
    public List<QuizResponseDTO> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(q -> getQuizById(q.getId()))
                .toList();
    }

    @Override
    public QuizSubmitResDTO submitQuiz(QuizSubmitReqDTO request) {
        // Lấy thông tin quiz và học sinh
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        Users student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        BigDecimal totalScore = BigDecimal.ZERO;
        for (QuizQuestion question : quiz.getQuestions()) {
            String userAnswer = request.getAnswers().get(question.getId());
            if (userAnswer != null && userAnswer.equalsIgnoreCase(String.valueOf(question.getCorrectOption()))) {
                totalScore = totalScore.add(question.getScore());
            }
        }

        QuizSubmission submission = new QuizSubmission();
        submission.setQuiz(quiz);
        submission.setStudent(student);
        submission.setStartAt(request.getStartAt());
        submission.setEndAt(request.getEndAt());
        submission.setSubmittedAt(request.getSubmittedAt());
        submission.setScore(totalScore);
        submission.setGradedAt(Instant.now());

        QuizSubmission saved = quizSubmissionRepository.save(submission);

        QuizSubmitResDTO res = new QuizSubmitResDTO();
        res.setId(saved.getId());
        res.setQuizId(quiz.getId());
        res.setStudentId(student.getId());
        res.setStudentName(student.getFullName());

        res.setQuizTitle(quiz.getTitle());
        res.setSubjectName(quiz.getSubject());

        if (quiz.getClassField() != null) {
            res.setClassName(quiz.getClassField().getClassName());
        } else {
            res.setClassName("Unknown");
        }

        res.setScore(saved.getScore());
        res.setStartAt(saved.getStartAt());
        res.setEndAt(saved.getEndAt());
        res.setGradedAt(saved.getGradedAt());
        return res;
    }

    @Override
    public List<QuizReportDTO> getReportByQuizId(Integer quizId) {
        return quizReportRepository.getQuizSubmissionReport(quizId);
    }

}
