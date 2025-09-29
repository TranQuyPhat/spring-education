package com.example.springboot_education.repositories.quiz;


import com.example.springboot_education.entities.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    @EntityGraph(attributePaths = {"questions"})
    Optional<Quiz> findById(Long id);
    Page<Quiz> findByClassField_IdAndStartDateAfter(Integer classId, Instant now, Pageable pageable);
    Page<Quiz> findByClassField_IdAndEndDateBefore(Integer classId, Instant now, Pageable pageable);
    Page<Quiz> findByClassField_IdAndStartDateBeforeAndEndDateAfter(Integer classId, Instant now1, Instant now2, Pageable pageable);
    long countByClassField_IdAndStartDateAfter(Integer classId, Instant now);

    // Đếm quiz của 1 lớp có endDate < now (đã kết thúc)
    long countByClassField_IdAndEndDateBefore(Integer classId, Instant now);

    // Đếm quiz của 1 lớp đang mở:
    // startDate <= now và endDate >= now
    long countByClassField_IdAndStartDateBeforeAndEndDateAfter(
            Integer classId, Instant startNow, Instant endNow);
    List<Quiz> findByCreatedBy_Id(Integer teacherId);
    @Query(value = """
        SELECT 
            q.id as quiz_id,
            q.title,
            q.description,
            q.time_limit,
            q.start_date,
            q.end_date,
            q.grade,
            q.subject,
            c.class_name as class_name,
            q.created_at,
            q.updated_at
        FROM quizzes q
        INNER JOIN classes c ON q.class_id = c.id
        INNER JOIN class_user cu ON c.id = cu.class_id
        WHERE cu.student_id = :studentId
        ORDER BY q.created_at DESC
        """, nativeQuery = true)
    List<Object> findQuizzesByStudentId(@Param("studentId") Integer studentId);
    @Query(value = """
SELECT
    q.id,
    q.title,
    q.description,
    q.time_limit,
    q.start_date,
    q.end_date,
    q.subject,
    c.class_name,
    (
        SELECT COUNT(*) FROM quiz_questions qq WHERE qq.quiz_id = q.id
    ) AS total_questions,
    s.score                 AS student_score,
    s.submitted_at          AS student_submitted_at,
    CASE WHEN s.quiz_id IS NULL THEN 0 ELSE 1 END AS has_submission
FROM quizzes q
INNER JOIN classes c     ON q.class_id = c.id
INNER JOIN class_user cu ON c.id = cu.class_id
LEFT JOIN (
    SELECT *
    FROM (
        SELECT
            qs.quiz_id,
            qs.student_id,
            qs.score,
            qs.submitted_at,
            qs.graded_at,
            ROW_NUMBER() OVER (
                PARTITION BY qs.quiz_id
                ORDER BY COALESCE(qs.graded_at, qs.submitted_at) DESC, qs.id DESC
            ) AS rn
        FROM quiz_submissions qs
        WHERE qs.student_id = :studentId
    ) t
    WHERE t.rn = 1
) s ON s.quiz_id = q.id
WHERE cu.student_id = :studentId
ORDER BY q.created_at DESC
""", nativeQuery = true)
    List<Object[]> findBasicQuizzesByStudentId(@Param("studentId") Integer studentId);



}