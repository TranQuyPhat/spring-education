package com.example.springboot_education.repositories.quiz;

import com.example.springboot_education.entities.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Integer> {
    int countByQuiz_Id(Integer quizId);
    @Query("SELECT qs FROM QuizSubmission qs " +
            "JOIN FETCH qs.student s " +
            "JOIN FETCH qs.quiz q " +
            "LEFT JOIN FETCH q.classField " +
            "WHERE q.id = :quizId")
    List<QuizSubmission> findAllByQuizIdWithDetails(@Param("quizId") Integer quizId);
    List<QuizSubmission> findByQuiz_Id(Integer quizId);

    @Query("""
        SELECT COALESCE(AVG(q.score), 0)
        FROM QuizSubmission q
        WHERE q.student.id = :studentId
        AND (:subjectId IS NULL OR q.quiz.classField.subject.id = :subjectId)
        AND (:classId IS NULL OR q.quiz.classField.id = :classId)
        AND (:startDate IS NULL OR q.submittedAt >= :startDate)
        AND (:endDate IS NULL OR q.submittedAt <= :endDate)
    """)
    BigDecimal avgQuizScore1(
            @Param("studentId") Integer studentId,
            @Param("subjectId") Integer subjectId,
            @Param("classId") Integer classId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("""
    SELECT q.student.id, q.student.fullName
    FROM QuizSubmission q
    GROUP BY q.student.id, q.student.fullName
""")
    List<Object[]> findAllStudentsForRanking();

    // Lấy chi tiết quiz của 1 học sinh
    @Query("""
    SELECT q.quiz.id, q.quiz.title, q.score
    FROM QuizSubmission q
    WHERE q.student.id = :studentId
""")
    List<Object[]> findQuizzesByStudent(@Param("studentId") Integer studentId);

    // Phân tích theo môn
    @Query("""
    SELECT q.quiz.classField.subject.id, q.quiz.classField.subject.subjectName, COALESCE(AVG(q.score), 0)
    FROM QuizSubmission q
    GROUP BY q.quiz.classField.subject.id, q.quiz.classField.subject.subjectName
""")
    List<Object[]> avgQuizBySubject();

    // Trends theo thời gian
    @Query("""
    SELECT FUNCTION('DATE', q.submittedAt), COALESCE(AVG(q.score), 0)
    FROM QuizSubmission q
    GROUP BY FUNCTION('DATE', q.submittedAt)
    ORDER BY FUNCTION('DATE', q.submittedAt)
""")
    List<Object[]> avgQuizByDate();

    @Query("""
      SELECT AVG(qs.score)
      FROM QuizSubmission qs
      WHERE qs.student.id = :studentId
        AND (:subjectId IS NULL OR qs.quiz.classField.subject.id = :subjectId)
        AND (:classId IS NULL OR qs.quiz.classField.id = :classId)
        AND (:startDate IS NULL OR qs.submittedAt >= :startDate)
        AND (:endDate IS NULL OR qs.submittedAt <= :endDate)
    """)
    Double avgQuizScore(
            @Param("studentId") Integer studentId,
            @Param("subjectId") Integer subjectId,
            @Param("classId") Integer classId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COUNT(qs)
        FROM QuizSubmission qs
        WHERE qs.student.id = :studentId
          AND (:subjectId IS NULL OR qs.quiz.classField.subject.id = :subjectId)
          AND (:startDateTime IS NULL OR qs.submittedAt >= :startDateTime)
          AND (:endDateTime IS NULL OR qs.submittedAt <= :endDateTime)
    """)
    Long countQuizzesTotal(Integer studentId, Integer subjectId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("""
        SELECT COUNT(qs)
        FROM QuizSubmission qs
        WHERE qs.student.id = :studentId
          AND (:subjectId IS NULL OR qs.quiz.classField.subject.id = :subjectId)
          AND qs.score IS NOT NULL
          AND (:startDateTime IS NULL OR qs.submittedAt >= :startDateTime)
          AND (:endDateTime IS NULL OR qs.submittedAt <= :endDateTime)
    """)
    Long countQuizzesGraded(Integer studentId, Integer subjectId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("""
        SELECT qs.score
        FROM QuizSubmission qs
        WHERE qs.student.id = :studentId
          AND (:subjectId IS NULL OR qs.quiz.classField.subject.id = :subjectId)
          AND qs.score IS NOT NULL
          AND (:startDateTime IS NULL OR qs.submittedAt >= :startDateTime)
          AND (:endDateTime IS NULL OR qs.submittedAt <= :endDateTime)
    """)
    List<Double> findQuizScores(Integer studentId, Integer subjectId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("""
      SELECT qs.quiz.id, qs.quiz.title, qs.score, qs.submittedAt
      FROM QuizSubmission qs
      WHERE qs.student.id = :studentId
        AND (:subjectId IS NULL OR qs.quiz.classField.subject.id = :subjectId)
      ORDER BY qs.submittedAt DESC
    """)
    List<Object[]> findQuizDetails(@Param("studentId") Integer studentId,
                                   @Param("subjectId") Integer subjectId);

    // For trends: average quiz score by date (returns Object[] {LocalDate, Double})
    @Query("""
      SELECT FUNCTION('DATE', qs.submittedAt), AVG(qs.score)
      FROM QuizSubmission qs
      WHERE qs.student.id = :studentId
        AND (:subjectId IS NULL OR qs.quiz.classField.subject.id = :subjectId)
      GROUP BY FUNCTION('DATE', qs.submittedAt)
      ORDER BY FUNCTION('DATE', qs.submittedAt)
    """)
    List<Object[]> avgQuizScoreByDate(@Param("studentId") Integer studentId,
                                      @Param("subjectId") Integer subjectId);

    //    grade student
    List<QuizSubmission> findByStudentIdAndQuiz_ClassField_Id(Integer studentId, Integer classId);
}
