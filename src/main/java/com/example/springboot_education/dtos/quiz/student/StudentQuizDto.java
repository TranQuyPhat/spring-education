package com.example.springboot_education.dtos.quiz.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentQuizDto {

        private Integer quizId;
        private String title;
        private String description;
        private Integer timeLimit;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String grade;
        private String subject;
        private String className;
        private LocalDateTime createdAt;
        private LocalDateTime updateAt;
}
