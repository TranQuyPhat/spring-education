package com.example.springboot_education.dtos.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassQuizDTO {
    private Integer classId;
    private String className;
    private String subjectName;

    // Tổng số quiz của lớp (theo status filter) – để FE biết còn bao nhiêu quiz nữa
    private long quizTotal;

    // 3 quiz đầu tiên (hoặc pageSize bạn truyền vào API)
    private List<QuizDTO> quizzes;
}