package com.example.springboot_education.services.quiz;


import com.example.springboot_education.entities.Quiz;
import com.example.springboot_education.exceptions.ResourceNotFoundException;
import com.example.springboot_education.repositories.quiz.QuizRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class QuizAccessService {

    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository submissionRepository;

    /**
     * Kiểm tra quyền truy cập quiz cho học sinh.
     * Ném BadRequestException nếu vi phạm.
     */
    public Quiz assertStudentCanAccess(Integer quizId, Integer studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz không tồn tại"));

        Instant now = Instant.now();

        if (quiz.getStartDate() != null &&
                now.isBefore(quiz.getStartDate().atZone(ZoneId.systemDefault()).toInstant())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quiz chưa mở.");
        }
        if (quiz.getEndDate() != null &&
                now.isAfter(quiz.getEndDate().atZone(ZoneId.systemDefault()).toInstant())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Quiz đã đóng.");
        }
        if (submissionRepository.existsByQuizIdAndStudentId(quizId, studentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Bạn đã nộp bài và không thể làm lại.");
        }

        return quiz;
    }
}
