package com.example.springboot_education.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.springboot_education.entities.ClassUser;
import com.example.springboot_education.entities.ClassUserId;

@Repository
public interface ClassUserRepository extends JpaRepository<ClassUser, ClassUserId> {
    int countByClassField_Id(Integer classId);
    List<ClassUser> findByClassField_Id(Integer classId);

    // Lấy danh sách class theo student
    List<ClassUser> findByStudent_Id(Integer studentId);
    Page<ClassUser> findByStudent_Id(Integer studentId, Pageable pageable);

    // Kiểm tra xem student đã thuộc class chưa
    boolean existsByClassField_IdAndStudent_Id(Integer classId, Integer studentId);

    // classAvg: average weighted score of all students in a class for given subject (null subject = class subject)
    @Query(nativeQuery = true, value = """
      SELECT AVG(weighted) FROM (
        SELECT COALESCE(a.avg_assign,0)*0.4 + COALESCE(q.avg_quiz,0)*0.6 AS weighted
        FROM (
          SELECT student_id,
                 AVG(score) as avg_assign
          FROM submissions s
          JOIN assignments a ON s.assignment_id = a.id
          JOIN classes c ON a.class_id = c.id
          WHERE c.id = :classId
            AND (:subjectId IS NULL OR c.subject_id = :subjectId)
          GROUP BY student_id
        ) AS asg
        FULL JOIN (
          SELECT student_id,
                 AVG(score) as avg_quiz
          FROM quiz_submissions qs
          JOIN quizzes q ON qs.quiz_id = q.id
          JOIN classes c2 ON q.class_id = c2.id
          WHERE c2.id = :classId
            AND (:subjectId IS NULL OR c2.subject_id = :subjectId)
          GROUP BY student_id
        ) AS qz ON asg.student_id = qz.student_id
      ) t
    """)
    Double classWeightedAvg(@Param("classId") Integer classId, @Param("subjectId") Integer subjectId);

}
