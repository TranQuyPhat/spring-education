package com.example.springboot_education.controllers.quiz;

import com.example.springboot_education.annotations.CurrentUser;
import com.example.springboot_education.dtos.quiz.submit.QuizSubmissionBaseDTO;
import com.example.springboot_education.dtos.quiz.submit.QuizSubmitReqDTO;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.services.quiz.QuizSubmitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-submissions")
@RequiredArgsConstructor
public class QuizSubmitController {
    private final QuizSubmitService quizSubmitService;
    @PostMapping
    public QuizSubmissionBaseDTO submitQuiz(
           @Valid @RequestBody QuizSubmitReqDTO dto,
            @CurrentUser Users currentUser
    ) {
        return quizSubmitService.submitQuiz(dto.getQuizId(), currentUser.getId(), dto);
    }
    @GetMapping("by-quiz/{quizId}")
    public ResponseEntity<List<QuizSubmissionBaseDTO>> getSubmissionsByQuiz(@PathVariable Integer quizId) {
        List<QuizSubmissionBaseDTO> submissions = quizSubmitService.getSubmissionsByQuizId(quizId);
        return ResponseEntity.ok(submissions);
    }

}
