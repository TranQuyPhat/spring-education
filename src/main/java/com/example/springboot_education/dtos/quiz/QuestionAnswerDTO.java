package com.example.springboot_education.dtos.quiz;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionAnswerDTO {
    private Integer questionId;
    private String content;
    private List<String> options; // hoặc dùng Map<"A", "Đáp án A"> nếu có thứ tự
    private String correctOption;
    private String studentAnswer;
}
