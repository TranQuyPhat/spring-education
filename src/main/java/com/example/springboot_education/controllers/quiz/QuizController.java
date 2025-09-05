package com.example.springboot_education.controllers.quiz;


import com.example.springboot_education.annotations.CurrentUser;
import com.example.springboot_education.dtos.quiz.APIResponse;
import com.example.springboot_education.dtos.quiz.QuizContentUpdateDTO;
import com.example.springboot_education.dtos.quiz.QuizRequestDTO;
import com.example.springboot_education.dtos.quiz.base.QuizBaseDTO;
import com.example.springboot_education.dtos.quiz.student.QuizResponseStudentDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuizResponseTeacherDTO;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.services.quiz.QuizService;
import com.example.springboot_education.services.quiz.impl.QuizServiceImpl;
import com.example.springboot_education.untils.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtTokenUtil jwtTokenUtil;
    @PostMapping
    public ResponseEntity<QuizBaseDTO> createQuiz(@RequestBody QuizRequestDTO request) {
        return ResponseEntity.ok(quizService.createQuiz(request));
    }
    @GetMapping("/{quizId}")
    public ResponseEntity<?> getQuizByRole(
            @PathVariable Integer quizId,
            @CurrentUser Users currentUser) {
        try {
            var roles = currentUser.getUserRoles();
            boolean isTeacher = roles.stream().anyMatch(r -> "teacher".equalsIgnoreCase(r.getRole().getName()));
            boolean isStudent = roles.stream().anyMatch(r -> "student".equalsIgnoreCase(r.getRole().getName()));

            if (isTeacher) {
                var teacherDTO = quizService.getQuizForTeacher(quizId);
                return ResponseEntity.ok(new APIResponse<>(true, "Dành cho giáo viên", teacherDTO));
            } else if (isStudent) {
                var studentDTO = ((QuizServiceImpl)quizService).getQuizForStudent(quizId, currentUser.getId());
                return ResponseEntity.ok(new APIResponse<>(true, "Dành cho học sinh", studentDTO));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new APIResponse<>(false, "Không có quyền truy cập", null));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new APIResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    @GetMapping("/teacher")
    public ResponseEntity<APIResponse<List<QuizResponseTeacherDTO>>> getQuizzesByCurrentTeacher(
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(new APIResponse<>(false, "Missing or invalid token", null));
            }
            token = token.substring(7); // Remove "Bearer "

            Integer teacherId = jwtTokenUtil.getUserIdFromToken(token);

            List<QuizResponseTeacherDTO> quizzes = quizService.getQuizzesByTeacherId(teacherId);

            return ResponseEntity.ok(new APIResponse<>(
                    true,
                    "Lấy danh sách quiz của giáo viên thành công",
                    quizzes
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new APIResponse<>(
                    false,
                    "Lỗi: " + e.getMessage(),
                    null
            ));
        }
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
            @CurrentUser Users currentUser
    ) {
        var roles = currentUser.getUserRoles();
        boolean isTeacher = roles.stream().anyMatch(r -> "teacher".equalsIgnoreCase(r.getRole().getName()));
        boolean isStudent = roles.stream().anyMatch(r -> "student".equalsIgnoreCase(r.getRole().getName()));

        if (isTeacher) {
            var dto = quizService.getQuizQuestionsPageForTeacher(id, page, size);
            return ResponseEntity.ok(dto);
        } else if (isStudent) {
            // dùng method có guard
            var dto = ((QuizServiceImpl)quizService).getQuizQuestionsPageForStudent(id, page, size, currentUser.getId());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("You don't have permission to access this quiz questions page");
    }


    //    @GetMapping("/{id}")
        //public ResponseEntity<?> getQuizByRole(
        //        @PathVariable Integer id,
        //        @RequestParam(defaultValue = "student") String role
        //) {
        //    if ("teacher".equalsIgnoreCase(role)) {
        //        return ResponseEntity.ok(quizService.getQuizForTeacher(id));
        //    } else if ("student".equalsIgnoreCase(role)) {
        //        return ResponseEntity.ok(quizService.getQuizForStudent(id));
        //    } else {
        //        return ResponseEntity.badRequest().body("Invalid role: " + role);
        //    }
        //}
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
    @GetMapping("/student")
    public ResponseEntity<APIResponse<List<QuizResponseStudentDTO>>> getStudentQuizzes(@CurrentUser Users student) {
        List<QuizResponseStudentDTO> quizzes = quizService.getQuizzesByStudentId(student.getId());

        return ResponseEntity.ok(new APIResponse<>(
                true,
                "Lấy danh sách quiz của học sinh thành công",
                quizzes
        ));
    }

    /**
     * Lấy danh sách quiz với phân trang
     */
    @GetMapping("/student/{studentId}/page")
    public ResponseEntity<APIResponse<List<QuizResponseStudentDTO>>> getStudentQuizzesWithPagination(
            @PathVariable Integer studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            // Có thể implement thêm logic phân trang ở đây
            List<QuizResponseStudentDTO> allQuizzes = quizService.getQuizzesByStudentId(studentId);

            int start = page * size;
            int end = Math.min(start + size, allQuizzes.size());

            List<QuizResponseStudentDTO> pagedQuizzes = start < allQuizzes.size() ?
                    allQuizzes.subList(start, end) : List.of();

            APIResponse<List<QuizResponseStudentDTO>> response = new APIResponse<>(
                    true,
                    "Lấy danh sách quiz thành công (trang " + (page + 1) + ")",
                    pagedQuizzes
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            APIResponse<List<QuizResponseStudentDTO>> response = new APIResponse<>(
                    false,
                    "Có lỗi xảy ra: " + e.getMessage(),
                    null
            );

            return ResponseEntity.internalServerError().body(response);
        }
    }
}
