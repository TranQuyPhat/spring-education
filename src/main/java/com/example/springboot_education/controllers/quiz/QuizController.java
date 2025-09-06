package com.example.springboot_education.controllers.quiz;


import com.example.springboot_education.dtos.quiz.APIResponse;
import com.example.springboot_education.dtos.quiz.QuestionsPageResponseDTO;
import com.example.springboot_education.dtos.quiz.QuizContentUpdateDTO;
import com.example.springboot_education.dtos.quiz.QuizRequestDTO;
import com.example.springboot_education.dtos.quiz.base.QuizBaseDTO;
import com.example.springboot_education.dtos.quiz.student.QuestionStudentDTO;
import com.example.springboot_education.dtos.quiz.student.StudentQuizDto;
import com.example.springboot_education.dtos.quiz.teacher.QuestionTeacherDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuizResponseTeacherDTO;
import com.example.springboot_education.services.quiz.QuizService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ResponseEntity<QuizBaseDTO> createQuiz(@Valid @RequestBody QuizRequestDTO request) {
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

    @GetMapping("/{id}/questions")
    public ResponseEntity<?> getQuestionsPage(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        var authorities = authentication.getAuthorities();
        boolean isTeacher = authorities.stream().anyMatch(a -> a.getAuthority().equals("teacher"));
        boolean isStudent = authorities.stream().anyMatch(a -> a.getAuthority().equals("student"));

        if (isTeacher) {
            QuestionsPageResponseDTO<QuestionTeacherDTO> dto =
                    quizService.getQuizQuestionsPageForTeacher(id, page, size);
            return ResponseEntity.ok(dto);
        } else if (isStudent) {
            QuestionsPageResponseDTO<QuestionStudentDTO> dto =
                    quizService.getQuizQuestionsPageForStudent(id, page, size);
            return ResponseEntity.ok(dto);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("You don't have permission to access this quiz questions page");
    }

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
                                             @Valid @RequestBody QuizBaseDTO dto) {
        return quizService.updateQuizMeta(id, dto);
    }

    @PutMapping("/{id}/content")
    public QuizResponseTeacherDTO replaceContent(@PathVariable Integer id,
                                                @Valid @RequestBody QuizContentUpdateDTO dto) {
        dto.setReplaceAll(true);
        return quizService.updateQuizContent(id, dto);
    }

    @PatchMapping("/{id}/content")
    public QuizResponseTeacherDTO upsertContent(@PathVariable Integer id,
                                               @Valid @RequestBody QuizContentUpdateDTO dto) {
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
    @GetMapping("/student/{studentId}")
    public ResponseEntity<APIResponse<List<StudentQuizDto>>> getStudentQuizzes(
            @PathVariable Integer studentId) {

        try {
            List<StudentQuizDto> quizzes = quizService.getQuizzesByStudentId(studentId);

            APIResponse<List<StudentQuizDto>> response = new APIResponse<>(
                    true,
                    "Lấy danh sách quiz thành công",
                    quizzes
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            APIResponse<List<StudentQuizDto>> response = new APIResponse<>(
                    false,
                    "Có lỗi xảy ra khi lấy danh sách quiz: " + e.getMessage(),
                    null
            );

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Lấy danh sách quiz với phân trang
     */
    @GetMapping("/student/{studentId}/page")
    public ResponseEntity<APIResponse<List<StudentQuizDto>>> getStudentQuizzesWithPagination(
            @PathVariable Integer studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            // Có thể implement thêm logic phân trang ở đây
            List<StudentQuizDto> allQuizzes = quizService.getQuizzesByStudentId(studentId);

            int start = page * size;
            int end = Math.min(start + size, allQuizzes.size());

            List<StudentQuizDto> pagedQuizzes = start < allQuizzes.size() ?
                    allQuizzes.subList(start, end) : List.of();

            APIResponse<List<StudentQuizDto>> response = new APIResponse<>(
                    true,
                    "Lấy danh sách quiz thành công (trang " + (page + 1) + ")",
                    pagedQuizzes
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            APIResponse<List<StudentQuizDto>> response = new APIResponse<>(
                    false,
                    "Có lỗi xảy ra: " + e.getMessage(),
                    null
            );

            return ResponseEntity.internalServerError().body(response);
        }
    }
}
