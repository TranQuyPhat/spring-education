package com.example.springboot_education.dtos.gradeDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDetailsDTO {
    private List<AssignmentDetailDTO> assignments;
    private List<QuizDetailDTO> quizzes;
}