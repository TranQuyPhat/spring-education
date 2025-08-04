package com.example.springboot_education.repositories.quiz;

import com.example.springboot_education.dtos.quiz.QuizReportDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface QuizReportRepository  {

    @Query(value = """
        SELECT
          q.id AS quizId,
          q.title AS quizTitle,
          q.start_date AS quizStartDate,
          q.end_date AS quizEndDate,
          q.subject AS subjectName,
          c.class_name AS className,
          (
            SELECT COUNT(*) FROM class_user cu2 WHERE cu2.class_id = c.id
          ) AS totalStudentsInClass,
          s.id AS submissionId,
          s.student_id AS studentId,
          u.full_name AS studentName,
          s.score AS score,
          s.start_at AS startAt,
          s.end_at AS endAt,
          s.submitted_at AS submittedAt,
          s.graded_at AS gradedAt
        FROM quizzes q
        JOIN quiz_submissions s ON s.quiz_id = q.id
        JOIN users u ON u.id = s.student_id
        JOIN classes c ON q.class_id = c.id
        WHERE q.id = :quizId
        ORDER BY s.submitted_at
        """, nativeQuery = true)
    List<QuizReportDTO> getQuizSubmissionReport(@Param("quizId") Integer quizId);
}