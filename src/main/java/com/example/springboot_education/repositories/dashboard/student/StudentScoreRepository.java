package com.example.springboot_education.repositories.dashboard.student;


import com.example.springboot_education.entities.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentScoreRepository extends JpaRepository<Submission, Integer> {

    // Query để lấy 5 điểm gần nhất từ cả Assignment và Quiz
    @Query(value = """

            (SELECT c.class_name, s.subject_name, sub.submitted_at, sub.score, 'ASSIGNMENT' as type, a.title
         FROM submissions sub
         JOIN assignments a ON sub.assignment_id = a.id
         JOIN classes c ON a.class_id = c.id
         JOIN subjects s ON c.subject_id = s.id
         WHERE sub.student_id = :studentId)
        UNION ALL
        (SELECT c.class_name, s.subject_name, qs.submitted_at, qs.score, 'QUIZ' as type, q.title
         FROM quiz_submissions qs
         JOIN quizzes q ON qs.quiz_id = q.id
         JOIN classes c ON q.class_id = c.id
         JOIN subjects s ON c.subject_id = s.id
         WHERE qs.student_id = :studentId)
        ORDER BY submitted_at DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> findRecentScoresByStudentId(@Param("studentId") Integer studentId);

    // Query để lấy tất cả kết quả của các lớp mà học sinh tham gia

    @Query(value = """
        (SELECT c.id as class_id, c.class_name, s.subject_name, a.title, 'assignment' as type, 
                sub.submitted_at, sub.score
         FROM submissions sub
         JOIN assignments a ON sub.assignment_id = a.id
         JOIN classes c ON a.class_id = c.id
         JOIN subjects s ON c.subject_id = s.id
         JOIN class_user cu ON cu.class_id = c.id
         JOIN user_roles ur ON ur.user_id = cu.student_id
         WHERE cu.student_id = :studentId 
           AND ur.role_id = 1 
           AND ur.enabled = 1
           AND sub.status = 'GRADED')
        UNION ALL
        (SELECT c.id as class_id, c.class_name, s.subject_name, q.title, 'quiz' as type,
                    qs.submitted_at, qs.score
             FROM quiz_submissions qs
             JOIN quizzes q ON qs.quiz_id = q.id
             JOIN classes c ON q.class_id = c.id
             JOIN subjects s ON c.subject_id = s.id
             JOIN class_user cu ON cu.class_id = c.id
             JOIN user_roles ur ON ur.user_id = cu.student_id
             WHERE cu.student_id = :studentId
               AND qs.student_id = :studentId
               AND ur.role_id = 1
               AND ur.enabled = 1)
        ORDER BY class_id, subject_name, submitted_at DESC
        """, nativeQuery = true)
    List<Object[]> findAllResultsByStudentId(@Param("studentId") Integer studentId);

    }

