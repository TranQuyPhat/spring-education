package com.example.springboot_education.repositories.assignment;

import com.example.springboot_education.entities.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionJpaRepository extends JpaRepository<Submission, Integer> {
    List<Submission> findByAssignmentId(Integer assignmentId);
    List<Submission> findByStudentId(Integer studentId);
    Optional<Submission> findByAssignmentIdAndStudentId(Integer assignmentId, Integer studentId);

    @Query("""
        SELECT COALESCE(AVG(s.score), 0)
        FROM Submission s
        WHERE s.student.id = :studentId
        AND (:subjectId IS NULL OR s.assignment.classField.subject.id = :subjectId)
        AND (:classId IS NULL OR s.assignment.classField.id = :classId)
        AND (:startDate IS NULL OR s.submittedAt >= :startDate)
        AND (:endDate IS NULL OR s.submittedAt <= :endDate)
    """)
    BigDecimal avgAssignmentScore1(
            @Param("studentId") Integer studentId,
            @Param("subjectId") Integer subjectId,
            @Param("classId") Integer classId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("""
        SELECT COUNT(s)
        FROM Submission s
        WHERE s.score IS NOT NULL
    """)
    long countGraded();

    @Query("""
        SELECT COUNT(s)
        FROM Submission s
        WHERE s.status = 'LATE'
    """)
    long countLate();

    @Query("""
        SELECT COUNT(s)
        FROM Submission s
        WHERE s.status = 'MISSING'
    """)
    long countMissing();

    @Query("""
    SELECT s.student.id, s.student.fullName
    FROM Submission s
    GROUP BY s.student.id, s.student.fullName
""")
    List<Object[]> findAllStudentsForRanking();

    // Lấy chi tiết bài tập của 1 học sinh
    @Query("""
    SELECT s.assignment.id, s.assignment.title, s.score, s.status
    FROM Submission s
    WHERE s.student.id = :studentId
""")
    List<Object[]> findAssignmentsByStudent(@Param("studentId") Integer studentId);

    // Phân tích theo môn
    @Query("""
    SELECT s.assignment.classField.subject.id, s.assignment.classField.subject.subjectName, COALESCE(AVG(s.score), 0)
    FROM Submission s
    GROUP BY s.assignment.classField.subject.id, s.assignment.classField.subject.subjectName
""")
    List<Object[]> avgAssignmentBySubject();

    // Trends theo thời gian
    @Query("""
    SELECT FUNCTION('DATE', s.submittedAt), COALESCE(AVG(s.score), 0)
    FROM Submission s
    GROUP BY FUNCTION('DATE', s.submittedAt)
    ORDER BY FUNCTION('DATE', s.submittedAt)
""")
    List<Object[]> avgAssignmentByDate();


    // average assignment score for a student (optionally filter by subject/class/date)
    @Query("""
      SELECT AVG(s.score)
      FROM Submission s
      WHERE s.student.id = :studentId
        AND (:subjectId IS NULL OR s.assignment.classField.subject.id = :subjectId)
        AND (:classId IS NULL OR s.assignment.classField.id = :classId)
        AND (:startDate IS NULL OR s.submittedAt >= :startDate)
        AND (:endDate IS NULL OR s.submittedAt <= :endDate)
    """)
    Double avgAssignmentScore(
            @Param("studentId") Integer studentId,
            @Param("subjectId") Integer subjectId,
            @Param("classId") Integer classId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

//    @Query("""
//      SELECT COUNT(s) FROM Submission s WHERE s.student.id = :studentId
//        AND (:subjectId IS NULL OR s.assignment.classField.subject.id = :subjectId)
//    """)
//    Long countAssignmentsTotal(@Param("studentId") Integer studentId,
//                               @Param("subjectId") Integer subjectId);
//
//    @Query("""
//      SELECT COUNT(s) FROM Submission s WHERE s.student.id = :studentId
//        AND s.score IS NOT NULL
//        AND (:subjectId IS NULL OR s.assignment.classField.subject.id = :subjectId)
//    """)
//    Long countAssignmentsGraded(@Param("studentId") Integer studentId,
//                                @Param("subjectId") Integer subjectId);
//
//    @Query("""
//      SELECT s.score FROM Submission s
//      WHERE s.student.id = :studentId
//        AND s.score IS NOT NULL
//        AND (:subjectId IS NULL OR s.assignment.classField.subject.id = :subjectId)
//    """)
//    List<Double> findAssignmentScores(@Param("studentId") Integer studentId,
//                                      @Param("subjectId") Integer subjectId);

    @Query("""
      SELECT s.assignment.id, s.assignment.title, s.score, s.status, s.assignment.dueDate
      FROM Submission s
      WHERE s.student.id = :studentId
        AND (:subjectId IS NULL OR s.assignment.classField.subject.id = :subjectId)
      ORDER BY s.assignment.dueDate DESC
    """)
    List<Object[]> findAssignmentDetails(@Param("studentId") Integer studentId,
                                         @Param("subjectId") Integer subjectId);


    @Query("""
  SELECT FUNCTION('DATE', s.submittedAt), AVG(s.score)
  FROM Submission s
  WHERE s.student.id = :studentId
    AND (:subjectId IS NULL OR s.assignment.classField.subject.id = :subjectId)
  GROUP BY FUNCTION('DATE', s.submittedAt)
  ORDER BY FUNCTION('DATE', s.submittedAt)
""")
    List<Object[]> findAvgAssignmentByDate(@Param("studentId") Integer studentId,
                                           @Param("subjectId") Integer subjectId);

    @Query("""
    SELECT COUNT(s)
    FROM Submission s
    WHERE s.student.id = :studentId
      AND (:subjectId IS NULL OR s.assignment.classField.subject.id = :subjectId)
      AND (:startDateTime IS NULL OR s.submittedAt >= :startDateTime)
      AND (:endDateTime IS NULL OR s.submittedAt <= :endDateTime)
""")
    Long countAssignmentsTotal(Integer studentId, Integer subjectId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("""
    SELECT COUNT(s)
    FROM Submission s
    WHERE s.student.id = :studentId
      AND (:subjectId IS NULL OR s.assignment.classField.subject.id = :subjectId)
      AND s.score IS NOT NULL
      AND (:startDateTime IS NULL OR s.submittedAt >= :startDateTime)
      AND (:endDateTime IS NULL OR s.submittedAt <= :endDateTime)
""")
    Long countAssignmentsGraded(Integer studentId, Integer subjectId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("""
    SELECT s.score
    FROM Submission s
    WHERE s.student.id = :studentId
      AND (:subjectId IS NULL OR s.assignment.classField.subject.id = :subjectId)
      AND s.score IS NOT NULL
      AND (:startDateTime IS NULL OR s.submittedAt >= :startDateTime)
      AND (:endDateTime IS NULL OR s.submittedAt <= :endDateTime)
""")
    List<Double> findAssignmentScores(Integer studentId, Integer subjectId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    // grade student
    List<Submission> findByStudentIdAndAssignment_ClassField_Id(Integer studentId, Integer classId);
}
