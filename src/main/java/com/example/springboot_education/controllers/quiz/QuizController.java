package com.example.springboot_education.controllers.quiz;


import com.example.springboot_education.dtos.quiz.QuizContentUpdateDTO;
import com.example.springboot_education.dtos.quiz.QuizRequestDTO;
import com.example.springboot_education.dtos.quiz.base.QuizBaseDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuizResponseTeacherDTO;
import com.example.springboot_education.services.quiz.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
//    public ResponseEntity<?> getQuizByRole(
//            @PathVariable Integer id,
//           Authentication authentication
//    ) {
//        String username=authentication.getName();
//        Collection<?extends GrantedAuthority> authorities=authentication.getAuthorities();
//        boolean isTeacher = authorities.stream()
//                .anyMatch(auth -> auth.getAuthority().equals("teacher"));
//        boolean isStudent = authorities.stream()
//                .anyMatch(auth -> auth.getAuthority().equals("student"));
//
//        if (isTeacher) {
//            return ResponseEntity.ok(quizService.getQuizForTeacher(id));
//        } else if (isStudent) {
//            return ResponseEntity.ok(quizService.getQuizForStudent(id));
//        } else {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body("You don't have permission to access this quiz");
//        }
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
    public ResponseEntity<List<QuizResponseTeacherDTO>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @PatchMapping("/{id}")
    public QuizResponseTeacherDTO updateMeta(@PathVariable Integer id,
                                             @RequestBody QuizBaseDTO dto) {
        return quizService.updateQuizMeta(id, dto);
    }

    @PutMapping("/{id}/content")
    public QuizResponseTeacherDTO replaceContent(@PathVariable Integer id,
                                                 @RequestBody QuizContentUpdateDTO dto) {
        dto.setReplaceAll(true);
        return quizService.updateQuizContent(id, dto);
    }

    @PatchMapping("/{id}/content")
    public QuizResponseTeacherDTO upsertContent(@PathVariable Integer id,
                                                @RequestBody QuizContentUpdateDTO dto) {
        dto.setReplaceAll(false);
        return quizService.updateQuizContent(id, dto);
    }

    @DeleteMapping("/{quizId}/questions/{questionId}")
    public void deleteQuestion(@PathVariable Integer quizId, @PathVariable Integer questionId) {
        quizService.deleteQuestion(quizId, questionId);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuiz(@PathVariable Integer id) {
        quizService.deleteQuiz(id);
    }
}
