package com.example.springboot_education.dtos.quiz;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizResponseTeacherDTO extends QuizResponseDTO {
    private List<QuestionResponseTeacherDTO> questions;
    private String className;
    private int totalStudents;
    private int studentsSubmitted;
}

