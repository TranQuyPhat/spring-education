package com.example.springboot_education.services.quiz;


import com.example.springboot_education.entities.Quiz;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.exceptions.ResourceNotFoundException;
import com.example.springboot_education.repositories.quiz.QuizRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


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
                .orElseThrow(() -> new EntityNotFoundException("Quiz"));

        Instant now = Instant.now();

        if (quiz.getStartDate() != null &&
                now.isBefore(quiz.getStartDate().atZone(ZoneId.systemDefault()).toInstant())) {
            throw new HttpException( "Quiz chưa mở.", HttpStatus.BAD_REQUEST);
        }
        if (quiz.getEndDate() != null &&
                now.isAfter(quiz.getEndDate().atZone(ZoneId.systemDefault()).toInstant())) {
            throw new HttpException("Quiz đã đóng.", HttpStatus.BAD_REQUEST);
        }
        if (submissionRepository.existsByQuizIdAndStudentId(quizId, studentId)) {
            throw new HttpException("Bạn đã nộp bài và không thể làm lại.", HttpStatus.BAD_REQUEST);
        }

        return quiz;
    }
}
