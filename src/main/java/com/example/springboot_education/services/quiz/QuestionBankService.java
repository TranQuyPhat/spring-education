package com.example.springboot_education.services.quiz;

import java.util.List;

public interface QuestionBankService {
    /**
     * Export questions from a quiz into the question bank. If questionIds is null
     * or empty, export all questions in the quiz.
     * 
     * @param quizId      ID của quiz
     * @param questionIds Danh sách câu hỏi muốn export (null = export tất cả)
     * @param userId      ID của người phê duyệt (lấy từ token)
     * @param isAdmin     True nếu user có quyền ADMIN
     * @return Số lượng câu hỏi đã tạo trong question bank
     * @throws RuntimeException nếu quiz không tồn tại hoặc user không có quyền
     */
    int publishQuizQuestionsToBank(Integer quizId, List<Integer> questionIds, Integer userId, boolean isAdmin);
}
