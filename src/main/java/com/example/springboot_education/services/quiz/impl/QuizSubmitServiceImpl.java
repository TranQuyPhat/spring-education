package com.example.springboot_education.services.quiz.impl;

import com.example.springboot_education.dtos.notification.NotificationTeacherDTO;
import com.example.springboot_education.dtos.quiz.submit.QuizSubmissionBaseDTO;
import com.example.springboot_education.dtos.quiz.submit.QuizSubmitReqDTO;
import com.example.springboot_education.entities.*;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.quiz.QuizAnswerRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import com.example.springboot_education.services.SlackService;
import com.example.springboot_education.services.classes.NotificationService;
import com.example.springboot_education.services.quiz.QuizAccessService;
import com.example.springboot_education.services.quiz.QuizSubmitService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizSubmitServiceImpl implements QuizSubmitService {

    private static final Logger log = LoggerFactory.getLogger(QuizSubmitServiceImpl.class);
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final UsersJpaRepository userRepository;
    private final QuizAccessService quizAccessService;
    private final SlackService slackService;
    private final NotificationService notificationService;


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
                    quiz.getClassField() != null ? quiz.getClassField().getClassName() : "Unknown");
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
    public QuizSubmissionBaseDTO submitQuiz(Integer quizId, Integer studentId, QuizSubmitReqDTO request) {
        Quiz quiz = quizAccessService.assertStudentCanAccess(quizId, studentId);
        Users student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student"));

        QuizSubmission submission = new QuizSubmission();
        submission.setQuiz(quiz);
        submission.setStudent(student);
        submission.setStartAt(request.getStartAt());
        submission.setEndAt(request.getEndAt());
        submission.setSubmittedAt(Instant.now());
        submission = quizSubmissionRepository.save(submission);

        BigDecimal totalScore = BigDecimal.ZERO;
        int correctCount = 0;
        int totalQuestions = quiz.getQuestions().size();

        List<QuizAnswer> answers = new ArrayList<>(totalQuestions);
        for (QuizQuestion question : quiz.getQuestions()) {
            List<String> userAnswers = request.getAnswers().get(question.getId());

            String userAnswerStr = userAnswers != null ? String.join(",", userAnswers) : null;

            boolean isCorrect = checkAnswer(userAnswers, question.getCorrectOptions());

            if (isCorrect) {
                correctCount++;
            }

            QuizAnswer answer = new QuizAnswer();
            answer.setSubmission(submission);
            answer.setQuestion(question);
            answer.setSelectedOptions(userAnswerStr);
            answer.setIsCorrect(isCorrect);
            answers.add(answer);
        }
        double percentage = (double) correctCount / totalQuestions;
        totalScore = BigDecimal.valueOf(percentage * 10).setScale(2, RoundingMode.HALF_UP);
        quizAnswerRepository.saveAll(answers);
        submission.setScore(totalScore);
        submission.setGradedAt(Instant.now());
        submission = quizSubmissionRepository.save(submission);
        Map<String,Object> payload = Map.of(
                "student",   student.getFullName(),
                "quizTitle", quiz.getTitle()
        );
        slackService.sendSlackNotification(
                quiz.getClassField().getId(),
                SlackService.ClassEventType.QUIZ_SUBMITTED,
                payload
        );
        NotificationTeacherDTO notifyPayload = NotificationTeacherDTO.builder()
                        .classId(quiz.getClassField().getId())
                        .studentName(student.getFullName())
                        .message("Có học sinh nộp bài quiz: " + quiz.getTitle())
                        .build();
        System.out.println("Notifying class ID: " + quiz.getClassField().getId() + " with payload: " + notifyPayload);

        notificationService.notifyTeacher(quiz.getCreatedBy().getId(), notifyPayload);

        return mapToDTO(submission, totalQuestions, correctCount);
    }

    // Phương thức kiểm tra đáp án mới
    private boolean checkAnswer(List<String> userAnswers, String correctOptions) {
        if (userAnswers == null || userAnswers.isEmpty() || correctOptions == null) {
            return false;
        }

        // Parse correct options
        Set<String> correctSet = parseOptions(correctOptions);

        // Parse user answers
        Set<String> userSet = userAnswers.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        // So sánh chính xác (All or Nothing)
        return correctSet.equals(userSet);
    }

    private Set<String> parseOptions(String options) {
        if (options == null || options.trim().isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(options.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
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
                        : "Unknown");
        dto.setScore(sub.getScore());
        dto.setStartAt(sub.getStartAt());
        dto.setEndAt(sub.getEndAt());
        dto.setSubmittedAt(sub.getSubmittedAt());
        dto.setGradedAt(sub.getGradedAt());
        return dto;
    }
}
