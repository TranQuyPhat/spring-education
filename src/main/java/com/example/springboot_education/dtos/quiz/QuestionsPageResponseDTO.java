package com.example.springboot_education.dtos.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionsPageResponseDTO<T> {
    private Integer quizId;
    private int page;   // 1-based
    private int size;
    private long total; // tổng số câu hỏi của quiz
    private List<T> items;


}