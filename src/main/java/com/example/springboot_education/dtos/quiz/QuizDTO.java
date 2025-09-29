package com.example.springboot_education.dtos.quiz;

import com.example.springboot_education.dtos.quiz.base.QuizBaseDTO;
import com.example.springboot_education.entities.Quiz;
import com.example.springboot_education.entities.QuizStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneOffset;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class QuizDTO extends QuizBaseDTO {
    private QuizStatus status;          // OPEN / UPCOMING / CLOSED

    private int totalStudents;
    private int studentsSubmitted;
    public static QuizDTO fromEntity(
            Quiz quiz,
            int totalStudents,
            int studentsSubmitted
    ) {
        QuizDTO dto = new QuizDTO();
        // ----- các field kế thừa từ QuizBaseDTO -----
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setTimeLimit(quiz.getTimeLimit());
        dto.setStartDate(
                quiz.getStartDate() != null ? quiz.getStartDate().atOffset(ZoneOffset.UTC) : null
        );
        dto.setEndDate(
                quiz.getEndDate() != null ? quiz.getEndDate().atOffset(ZoneOffset.UTC) : null
        );
        dto.setSubject(quiz.getSubject());
        dto.setClassName(quiz.getClassField().getClassName());
        dto.setTotalQuestion(
                quiz.getQuestions() != null ? quiz.getQuestions().size() : 0
        );

        // ----- field của QuizDTO -----
        dto.setStatus(quiz.getStatus()); // hoặc quiz.getStatus() nếu đã @Transient
        dto.setTotalStudents(totalStudents);
        dto.setStudentsSubmitted(studentsSubmitted);

        return dto;
    }
}