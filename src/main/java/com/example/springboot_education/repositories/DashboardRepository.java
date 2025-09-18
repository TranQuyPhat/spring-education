package com.example.springboot_education.repositories;

import com.example.springboot_education.dtos.dashboardsClient.ClassProgressDTO;
import com.example.springboot_education.entities.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardRepository extends JpaRepository<ClassEntity, Long> {

    // teacher
    // Tổng lớp dạy
    @Query("SELECT COUNT(c) FROM ClassEntity c WHERE c.teacher.id = :teacherId")
    long countClassesByTeacher(@Param("teacherId") Integer teacherId);

    // Tổng học sinh (distinct trong tất cả class dạy)
    @Query("SELECT COUNT(DISTINCT cu.student.id) " +
            "FROM ClassUser cu WHERE cu.classField.teacher.id = :teacherId")
    long countStudentsByTeacher(@Param("teacherId") Integer teacherId);

    // Tổng bài tập
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.classField.teacher.id = :teacherId")
    long countAssignmentsByTeacher(@Param("teacherId") Integer teacherId);

    // Bài cần chấm
    @Query("SELECT COUNT(s) FROM Submission s " +
            "WHERE s.assignment.classField.teacher.id = :teacherId AND s.score IS NULL")
    long countPendingGrading(@Param("teacherId") Integer teacherId);

    // Điểm trung bình
    @Query("SELECT AVG(s.score) FROM Submission s " +
            "WHERE s.assignment.classField.teacher.id = :teacherId AND s.score IS NOT NULL")
    Double findAverageScoreByTeacher(@Param("teacherId") Integer teacherId);

    // --- Recent activities ---
    // Submission gần đây của giáo viên
    @Query("""
                SELECT s.assignment.id, s.assignment.title, s.assignment.classField.className, COUNT(s.id), MAX(s.submittedAt)
                FROM Submission s
                WHERE s.assignment.classField.teacher.id = :teacherId
                GROUP BY s.assignment.id, s.assignment.title, s.assignment.classField.className
                ORDER BY MAX(s.submittedAt) DESC
            """)
    List<Object[]> countSubmissionsPerAssignmentByTeacher(@Param("teacherId") Integer teacherId);

    // Comment gần đây của giáo viên
    @Query("""
                SELECT c.id, c.user.fullName, c.assignment.title, c.assignment.classField.className, c.createdAt
                FROM AssignmentComment c
                WHERE c.assignment.classField.teacher.id = :teacherId
                ORDER BY c.createdAt DESC
            """)
    List<Object[]> findRecentCommentsByTeacher(@Param("teacherId") Integer teacherId);

    // student
    // Số lớp tham gia
    @Query("SELECT COUNT(cu) FROM ClassUser cu WHERE cu.student.id = :studentId")
    long countClassesByStudent(@Param("studentId") Integer studentId);

    // Tổng số bài tập (theo AssignmentJpaRepository)
    @Query("""
                SELECT COUNT(a)
                FROM Assignment a
                JOIN a.classField c
                JOIN c.classUsers cu
                WHERE cu.student.id = :studentId
            """)
    Long countAssignmentsByStudentId(@Param("studentId") Integer studentId);

    // Bài đã nộp (theo SubmissionJpaRepository)
    @Query("""
                SELECT COUNT(s)
                FROM Submission s
                WHERE s.student.id = :studentId
                  AND (:subjectId IS NULL OR s.assignment.classField.subject.id = :subjectId)
                  AND (:startDateTime IS NULL OR s.submittedAt >= :startDateTime)
                  AND (:endDateTime IS NULL OR s.submittedAt <= :endDateTime)
            """)
    Long countAssignmentsTotal(
            @Param("studentId") Integer studentId,
            @Param("subjectId") Integer subjectId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("""
                SELECT new com.example.springboot_education.dtos.dashboardsClient.ClassProgressDTO(
                    c.className,
                    COUNT(DISTINCT s.id),
                    COUNT(DISTINCT a.id)
                )
                FROM ClassEntity c
                JOIN c.classUsers cu
                JOIN Assignment a ON a.classField.id = c.id
                LEFT JOIN Submission s ON s.assignment.id = a.id AND s.student.id = :studentId
                WHERE cu.student.id = :studentId
                GROUP BY c.id, c.className
            """)
    List<ClassProgressDTO> findClassProgressByStudentId(@Param("studentId") Integer studentId);

    @Query(value = """
        SELECT
            SUM(CASE WHEN t.avg_score >= 9.0 THEN 1 ELSE 0 END) AS xuatSac,
            SUM(CASE WHEN t.avg_score BETWEEN 8.0 AND 8.9 THEN 1 ELSE 0 END) AS gioi,
            SUM(CASE WHEN t.avg_score BETWEEN 6.5 AND 7.9 THEN 1 ELSE 0 END) AS kha,
            SUM(CASE WHEN t.avg_score < 6.5 THEN 1 ELSE 0 END) AS canCaiThien,
            COUNT(*) AS totalStudents
        FROM (
            SELECT u.id AS student_id,
                   ROUND(AVG(all_scores.score),2) AS avg_score
            FROM users u
            JOIN (
                   SELECT s.student_id, s.score
                   FROM submissions s
                   JOIN assignments a ON s.assignment_id = a.id
                   JOIN classes c ON a.class_id = c.id
                   WHERE c.teacher_id = :teacherId
                     AND s.status = 'graded'
                   UNION ALL
                   SELECT qs.student_id, qs.score
                   FROM quiz_submissions qs
                   JOIN quizzes q ON qs.quiz_id = q.id
                   JOIN classes c2 ON q.class_id = c2.id
                   WHERE c2.teacher_id = :teacherId
                 ) AS all_scores
              ON u.id = all_scores.student_id
            GROUP BY u.id
        ) AS t
        """, nativeQuery = true)
    Object findGradeDistribution(@Param("teacherId") Long teacherId);
}
