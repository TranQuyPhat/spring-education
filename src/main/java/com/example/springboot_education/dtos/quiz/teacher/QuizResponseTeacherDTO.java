package com.example.springboot_education.dtos.quiz.teacher;

import com.example.springboot_education.dtos.quiz.QuizBaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizResponseTeacherDTO extends QuizBaseDTO {
    private Integer classId;
    private Integer createdBy;
    private String className;
    private List<QuestionTeacherDTO> questions;
    private int totalStudents;
    private int studentsSubmitted;
}

