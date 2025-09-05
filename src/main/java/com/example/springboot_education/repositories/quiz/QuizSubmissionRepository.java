package com.example.springboot_education.repositories.quiz;

import com.example.springboot_education.dtos.gradeDTOs.GradeBase.BaseScoreStatsDTO;
import com.example.springboot_education.dtos.gradeDTOs.GradeBase.QuizAverageScoreDTO;
import com.example.springboot_education.dtos.gradeDTOs.GradeBase.WeightedScorePerClassDTO;
import com.example.springboot_education.entities.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Query("SELECT qs.quiz.id FROM QuizSubmission qs WHERE qs.student.id = :studentId")
    List<Integer> findSubmittedQuizIdsByStudentId(@Param("studentId") Integer studentId);

    @Query("""
        SELECT new com.example.springboot_education.dtos.gradeDTOs.GradeBase.QuizAverageScoreDTO(
            qs.quiz.id, qs.student.fullName, AVG(qs.score)
        )
        FROM QuizSubmission qs
        WHERE qs.quiz.id = :quizId
        GROUP BY qs.quiz.id, qs.student.fullName
    """)
    List<QuizAverageScoreDTO> findAverageScorePerQuiz(@Param("quizId") Integer quizId);

    @Query("""
        SELECT new com.example.springboot_education.dtos.gradeDTOs.GradeBase.BaseScoreStatsDTO(
            qs.quiz.classField.id, qs.quiz.classField.className, qs.student.fullName,qs.student.email, AVG(qs.score)
        )
        FROM QuizSubmission qs
        WHERE qs.quiz.classField.id = :classId
        GROUP BY qs.quiz.classField.id, qs.quiz.classField.className, qs.student.fullName
    """)
    List<BaseScoreStatsDTO> findAverageScoreQuizByClass(@Param("classId") Integer classId);

    @Query("""
    SELECT new com.example.springboot_education.dtos.gradeDTOs.GradeBase.BaseScoreStatsDTO(
        qs.quiz.classField.id,
        qs.quiz.classField.className,
        qs.student.fullName,
        qs.student.email,
        AVG(qs.score)
    )
    FROM QuizSubmission qs
    WHERE qs.student.id = :studentId
    GROUP BY qs.quiz.classField.id, qs.quiz.classField.className, qs.student.fullName
""")
    List<BaseScoreStatsDTO> findQuizAvgPerClassByStudent(@Param("studentId") Integer studentId);
    @Query("""
    SELECT new com.example.springboot_education.dtos.gradeDTOs.GradeBase.WeightedScorePerClassDTO(
        qs.quiz.classField.id,
        qs.quiz.classField.className,
        qs.student.fullName,
        AVG(qs.score),
        null,
        null
    )
    FROM QuizSubmission qs
    WHERE qs.student.id = :studentId
    GROUP BY qs.quiz.classField.id, qs.quiz.classField.className, qs.student.fullName
""")
    List<WeightedScorePerClassDTO> findQuizAvgByClassAndStudent(@Param("studentId") Integer studentId);

    @Query("""
    SELECT new com.example.springboot_education.dtos.gradeDTOs.GradeBase.BaseScoreStatsDTO(
        c.id, c.className, s.fullName, s.email, AVG(qs.score)
    )
    FROM QuizSubmission qs
    JOIN qs.quiz q
    JOIN q.classField c
    JOIN c.teacher t
    JOIN qs.student s
    WHERE t.id = :teacherId
    GROUP BY c.id, c.className, s.fullName, s.email
""")
    List<BaseScoreStatsDTO> findQuizAverageByTeacher(@Param("teacherId") Integer teacherId);
    boolean existsByQuizIdAndStudentId(Integer quizId, Integer studentId);
}
