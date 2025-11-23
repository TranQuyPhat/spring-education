package com.example.springboot_education.controllers.quiz;

import com.example.springboot_education.annotations.CurrentUser;
import com.example.springboot_education.dtos.quiz.APIResponse;
import com.example.springboot_education.entities.Quiz;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.quiz.QuizRepository;
import com.example.springboot_education.services.quiz.QuestionBankService;
import com.example.springboot_education.untils.RoleUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question-bank")
@RequiredArgsConstructor
public class QuestionBankController {
    private final QuestionBankService questionBankService;
    private final QuizRepository quizRepository;

    @PostMapping("/publish-quiz/{quizId}")
    public ResponseEntity<APIResponse<?>> publishQuizQuestionsToBank(
            @PathVariable Integer quizId,
            @RequestBody(required = false) PublishQuizRequest body,
            @CurrentUser Users currentUser) {

        // Check quyền: user phải có quyền TEACHER hoặc ADMIN
        if (!RoleUtil.isTeacherOrAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new APIResponse<>(false, "Không có quyền thực hiện hành động này", null));
        }

        // Lấy quiz để check quyền ownership
        Quiz quiz = quizRepository.findById(quizId)
                .orElse(null);

        if (quiz == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new APIResponse<>(false, "Quiz không tồn tại", null));
        }

       
        boolean isAdmin = RoleUtil.isAdmin(currentUser);
        if (!isAdmin && !quiz.getCreatedBy().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new APIResponse<>(false, "Bạn không có quyền publish quiz này", null));
        }

        List<Integer> qids = body == null ? null : body.getQuestionIds();

        // Gọi service với userId và isAdmin
        int created = questionBankService.publishQuizQuestionsToBank(quizId, qids, currentUser.getId(), isAdmin);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("created", created);

        return ResponseEntity.ok(new APIResponse<>(true, "Publish quiz thành công", responseData));
    }

    @Data
    public static class PublishQuizRequest {
        private List<Integer> questionIds;
    }

}
