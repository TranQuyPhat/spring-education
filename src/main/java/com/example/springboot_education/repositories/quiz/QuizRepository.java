package com.example.springboot_education.repositories.quiz;


import com.example.springboot_education.entities.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    @EntityGraph(attributePaths = {"questions"})
    Optional<Quiz> findById(Long id);

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
    q.grade,
    q.subject,
    c.class_name,
    q.created_at,
    q.updated_at
    FROM quizzes q
    INNER JOIN classes c ON q.class_id = c.id
    INNER JOIN class_user cu ON c.id = cu.class_id
    WHERE cu.student_id = :studentId
    ORDER BY q.created_at DESC
    """, nativeQuery = true)
    List<Object[]> findBasicQuizzesByStudentId(@Param("studentId") Integer studentId);
}