package com.example.springboot_education.services.quiz.impl;

import com.example.springboot_education.dtos.quiz.submit.QuizSubmissionBaseDTO;
import com.example.springboot_education.dtos.quiz.submit.QuizSubmitReqDTO;
import com.example.springboot_education.entities.*;
import com.example.springboot_education.exceptions.ResourceNotFoundException;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.quiz.QuizAnswerRepository;
import com.example.springboot_education.repositories.quiz.QuizRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import com.example.springboot_education.services.ActivityLogService;
import com.example.springboot_education.services.quiz.QuizSubmitService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class QuizSubmitServiceImpl implements QuizSubmitService {

    private static final Logger log = LoggerFactory.getLogger(QuizSubmitServiceImpl.class);
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizRepository quizRepository;
    private final UsersJpaRepository userRepository;
    private final ActivityLogService activityLogService;
    @Override
    public List<QuizSubmissionBaseDTO> getSubmissionsByQuizId(Integer quizId) {
        List<QuizSubmission> submissions = quizSubmissionRepository.findAllByQuizIdWithDetails(quizId);

        return submissions.stream().map(submission -> {
            Quiz quiz = submission.getQuiz();
            Users student = submission.getStudent();

            QuizSubmissionBaseDTO res = new QuizSubmissionBaseDTO();
            res.setId(submission.getId());
            res.setQuizId(quiz.getId());
            res.setStudentId(student.getId());
            res.setStudentName(student.getFullName());
            res.setQuizTitle(quiz.getTitle());
            res.setSubjectName(quiz.getSubject());
            res.setClassName(
                    quiz.getClassField() != null ? quiz.getClassField().getClassName() : "Unknown"
            );
            res.setScore(submission.getScore());
            res.setStartAt(submission.getStartAt());
            res.setEndAt(submission.getEndAt());
            res.setGradedAt(submission.getGradedAt());
            res.setSubmittedAt(submission.getSubmittedAt());
            return res;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuizSubmissionBaseDTO submitQuiz(QuizSubmitReqDTO request) {
        // 1. Lấy quiz và student
        log.info("📥 quizId: {}", request.getQuizId());
        log.info("📥 studentId: {}", request.getStudentId());
        log.info("📥 startAt: {}", request.getStartAt());
        log.info("📥 answers: {}", request.getAnswers());
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        Users student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // 2. Tạo submission (chưa tính score)
        QuizSubmission submission = new QuizSubmission();
        submission.setQuiz(quiz);
        submission.setStudent(student);
        submission.setStartAt(request.getStartAt());
        submission.setEndAt(request.getEndAt());
        submission.setSubmittedAt(Instant.now());
        submission = quizSubmissionRepository.save(submission);

        // 3. Xử lý từng câu, tính tổng điểm và đếm số câu đúng
        BigDecimal totalScore = BigDecimal.ZERO;
        int correctCount = 0;
        int totalQuestions = quiz.getQuestions().size();

        List<QuizAnswer> answers = new ArrayList<>(totalQuestions);
        for (QuizQuestion question : quiz.getQuestions()) {
            String userAnswer = request.getAnswers().get(question.getId());
            boolean isCorrect = checkAnswer(userAnswer, question.getCorrectOption());

            if (isCorrect) {
                totalScore = totalScore.add(question.getScore());
                correctCount++;
            }

            QuizAnswer answer = new QuizAnswer();
            answer.setSubmission(submission);
            answer.setQuestion(question);
            answer.setSelectedOption(
                    userAnswer != null && !userAnswer.isEmpty()
                            ? userAnswer.charAt(0)
                            : null
            );
            answer.setIsCorrect(isCorrect);
            answers.add(answer);
        }
        quizAnswerRepository.saveAll(answers);

        submission.setScore(totalScore);
        submission.setGradedAt(Instant.now());
        submission = quizSubmissionRepository.save(submission);

        return mapToDTO(submission, totalQuestions, correctCount);
    }

    private boolean checkAnswer(String userAnswer, Character correctAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }

        // Loại bỏ khoảng trắng thừa
        String trimmed = userAnswer.trim();
        if (trimmed.length() != 1) {
            return false;
        }

        char userChar    = Character.toUpperCase(trimmed.charAt(0));
        char correctChar = Character.toUpperCase(correctAnswer);

        return userChar == correctChar;
    }



    private QuizSubmissionBaseDTO mapToDTO(QuizSubmission sub, int totalQ, int correctQ) {
        QuizSubmissionBaseDTO dto = new QuizSubmissionBaseDTO();
        dto.setId(sub.getId());
        dto.setQuizId(sub.getQuiz().getId());
        dto.setStudentId(sub.getStudent().getId());
        dto.setStudentName(sub.getStudent().getFullName());
        dto.setQuizTitle(sub.getQuiz().getTitle());
        dto.setSubjectName(sub.getQuiz().getSubject());
        dto.setClassName(
                sub.getQuiz().getClassField() != null
                        ? sub.getQuiz().getClassField().getClassName()
                        : "Unknown"
        );
        dto.setScore(sub.getScore());
        dto.setStartAt(sub.getStartAt());
        dto.setEndAt(sub.getEndAt());
        dto.setSubmittedAt(sub.getSubmittedAt());
        dto.setGradedAt(sub.getGradedAt());
        return dto;
    }
    private QuizSubmissionBaseDTO mapToBaseDTO(QuizSubmission submission) {
        Quiz quiz = submission.getQuiz();
        Users student = submission.getStudent();

        QuizSubmissionBaseDTO dto = new QuizSubmissionBaseDTO();
        dto.setId(submission.getId());
        dto.setQuizId(quiz.getId());
        dto.setQuizTitle(quiz.getTitle());

        dto.setStudentId(student.getId());
        dto.setStudentName(student.getFullName());

        dto.setScore(submission.getScore());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setStartAt(submission.getStartAt());
        dto.setEndAt(submission.getEndAt());
        dto.setGradedAt(submission.getGradedAt());
        return dto;
    }
}
