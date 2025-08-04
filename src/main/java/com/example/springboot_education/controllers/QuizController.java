package com.example.springboot_education.controllers;


import com.example.springboot_education.dtos.quiz.*;
import com.example.springboot_education.services.quiz.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ResponseEntity<QuizResponseDTO> createQuiz(@RequestBody QuizRequestDTO request) {
        return ResponseEntity.ok(quizService.createQuiz(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponseDTO> getQuizById(@PathVariable Integer id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    @GetMapping
    public ResponseEntity<List<QuizResponseDTO>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }
    @GetMapping("/{quizId}/submissions")
    public List<QuizReportDTO> getQuizReport(@PathVariable Integer quizId) {
        return quizService.getReportByQuizId(quizId);
    }
    @PostMapping("/submission")
    public QuizSubmitResDTO submitQuiz(@RequestBody QuizSubmitReqDTO dto) {
        return quizService.submitQuiz(dto);
    }
}
