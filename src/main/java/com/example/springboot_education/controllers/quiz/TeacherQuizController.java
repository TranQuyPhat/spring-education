package com.example.springboot_education.controllers.quiz;

import com.example.springboot_education.dtos.quiz.APIResponse;
import com.example.springboot_education.dtos.quiz.GroupedQuizDTO;
import com.example.springboot_education.dtos.quiz.QuizDTO;
import com.example.springboot_education.entities.QuizStatus;
import com.example.springboot_education.services.quiz.TeacherQuizService;
import com.example.springboot_education.untils.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/quizzes")
@RequiredArgsConstructor
public class TeacherQuizController {
    private final TeacherQuizService service;
    private final JwtTokenUtil jwt;

    @GetMapping("/group-by-class")
    public ResponseEntity<APIResponse<GroupedQuizDTO>> getGroupedQuizzes(
            @RequestParam QuizStatus status,
            @RequestParam int classPage,
            @RequestParam int classSize,
            @RequestParam(defaultValue = "3") int quizPageSize,
            HttpServletRequest request) {

        Integer teacherId = jwt.getUserIdFromToken(
                request.getHeader("Authorization").substring(7));

        GroupedQuizDTO result = service.getGroupedQuizzesByTeacher(
                teacherId, status, classPage, classSize, quizPageSize);

        return ResponseEntity.ok(new APIResponse<>(true, "OK", result));
    }
    @GetMapping("/class/{classId}")
    public ResponseEntity<APIResponse<Page<QuizDTO>>> getQuizzesByClass(
            @PathVariable Integer classId,
            @RequestParam QuizStatus status,
            @RequestParam int page,
            @RequestParam int size) {

        Page<QuizDTO> quizDtoPage =
                service.getQuizDtosByClassAndStatus(classId, status, page, size);

        return ResponseEntity.ok(new APIResponse<>(true, "OK", quizDtoPage));
    }

}
