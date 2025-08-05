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
    public ResponseEntity<QuizBaseDTO> createQuiz(@RequestBody QuizRequestDTO request) {
        return ResponseEntity.ok(quizService.createQuiz(request));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<QuizBaseDTO> getQuizById(@PathVariable Integer id) {
//        return ResponseEntity.ok(quizService.getQuizById(id));
//    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuizByRole(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "student") String role
    ) {
        if ("teacher".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(quizService.getQuizForTeacher(id));
        } else if ("student".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(quizService.getQuizForStudent(id));
        } else {
            return ResponseEntity.badRequest().body("Invalid role: " + role);
        }
    }
    @GetMapping
    public ResponseEntity<List<QuizBaseDTO>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }
   
    @PostMapping("/submission")
    public QuizSubmitResDTO submitQuiz(@RequestBody QuizSubmitReqDTO dto) {
        return quizService.submitQuiz(dto);
    }
}
