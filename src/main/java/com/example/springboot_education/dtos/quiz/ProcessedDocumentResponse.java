package com.example.springboot_education.dtos.quiz;

import com.example.springboot_education.dtos.quiz.base.QuestionBaseDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedDocumentResponse {
    private List<QuestionBaseDTO> questions;
}