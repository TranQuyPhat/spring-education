package com.example.springboot_education.dtos.quiz.teacher;

import com.example.springboot_education.dtos.quiz.base.QuestionBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false) // thêm dòng này để Lombok không gọi super
public class QuestionTeacherDTO extends QuestionBaseDTO {
    private String correctOptions;
    private Set<String> correctAnswerTexts;
    private String correctAnswerRegex;
    private boolean caseSensitive;
    private boolean trimWhitespace;
}