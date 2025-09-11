package com.example.springboot_education.repositories.assignment;

import com.example.springboot_education.dtos.gradeDTOs.GradeBase.BaseScoreStatsDTO;
import com.example.springboot_education.dtos.gradeDTOs.GradeBase.WeightedScorePerClassDTO;
import com.example.springboot_education.dtos.submissionDTOs.SubmissionResponseDto;
import com.example.springboot_education.entities.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionJpaRepository extends JpaRepository<Submission, Integer> {
    List<Submission> findByAssignment_Id(Integer assignmentId);
    List<Submission> findByStudentId(Integer studentId);
    Optional<Submission> findByAssignmentIdAndStudentId(Integer assignmentId, Integer studentId);

    @Query("SELECT s FROM Submission s WHERE s.assignment.classField.id = :classId")
    List<Submission> findByClassId(@Param("classId") Integer classId);

    @Query("SELECT s FROM Submission s WHERE s.assignment.classField.id = :classId AND s.student.id = :studentId")
    List<Submission> findByClassIdAndStudentId(@Param("classId") Integer classId,
                                               @Param("studentId") Integer studentId);

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
    SELECT s.assignment.id, s.assignment.title, s.score, s.status, s.submittedAt
    FROM Submission s
    WHERE s.student.id = :studentId
""")
    List<Object[]> findAssignmentsByStudent(@Param("studentId") Integer studentId);



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

    @Query("""
    SELECT new com.example.springboot_education.dtos.gradeDTOs.GradeBase.BaseScoreStatsDTO(
        s.assignment.classField.id,
        s.assignment.classField.className,
        s.student.fullName,
        s.student.email,
        AVG(s.score)
    )
    FROM Submission s
    WHERE s.assignment.classField.id = :classId
      AND s.student.id = :studentId
    GROUP BY s.assignment.classField.id, s.assignment.classField.className, s.student.fullName
""")
    BaseScoreStatsDTO findAssignmentAverageByClassAndStudent(
            @Param("classId") Integer classId,
            @Param("studentId") Integer studentId
    );
    @Query("""
    SELECT new com.example.springboot_education.dtos.gradeDTOs.GradeBase.BaseScoreStatsDTO(
        s.assignment.classField.id,
        s.assignment.classField.className,
        s.student.fullName,
        s.student.email,
        AVG(s.score)
    )
    FROM Submission s
    WHERE s.student.id = :studentId
    GROUP BY s.assignment.classField.id, s.assignment.classField.className, s.student.fullName
""")
    List<BaseScoreStatsDTO> findAssignmentAverageAllClassesByStudent(@Param("studentId") Integer studentId);
    @Query("""
    SELECT new com.example.springboot_education.dtos.gradeDTOs.GradeBase.WeightedScorePerClassDTO(
        s.assignment.classField.id,
        s.assignment.classField.className,
        s.student.fullName,
        null,
        AVG(s.score),
        null
    )
    FROM Submission s
    WHERE s.student.id = :studentId
    GROUP BY s.assignment.classField.id, s.assignment.classField.className, s.student.fullName
""")
    List<WeightedScorePerClassDTO> findAssignmentAvgByClassAndStudent(@Param("studentId") Integer studentId);
    @Query("""
    SELECT new com.example.springboot_education.dtos.gradeDTOs.GradeBase.BaseScoreStatsDTO(
        c.id, c.className, s.fullName, s.email, AVG(sub.score)
    )
    FROM Submission sub
    JOIN sub.assignment a
    JOIN a.classField c
    JOIN c.teacher t
    JOIN sub.student s
    WHERE t.id = :teacherId
    GROUP BY c.id, c.className, s.fullName, s.email
""")
    List<BaseScoreStatsDTO> findAssignmentAverageByTeacher(@Param("teacherId") Integer teacherId);

}
