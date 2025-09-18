package com.example.springboot_education.repositories.assignment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.springboot_education.entities.Assignment;

@Repository
public interface AssignmentJpaRepository extends JpaRepository<Assignment, Integer> {
  List<Assignment> findByClassField_Id(Integer classId);

  @Query("""
          SELECT a FROM Assignment a
          JOIN a.classField c
          JOIN c.classUsers cu
          WHERE cu.student.id = :studentId
            AND NOT EXISTS (
                SELECT s FROM Submission s
                WHERE s.assignment = a
                  AND s.student.id = :studentId
            )
          ORDER BY a.dueDate ASC
      """)
  List<Assignment> findAssignmentsByStudentId(@Param("studentId") Integer studentId);

  @Query("SELECT a FROM Assignment a JOIN a.classField c WHERE c.teacher.id = :teacherId ORDER BY a.dueDate ASC")
  List<Assignment> findAssignmentsByTeacherId(@Param("teacherId") Integer teacherId);

  // Bài tập hết hạn hôm nay
  @Query("""
          SELECT a FROM Assignment a
          WHERE DATE(a.dueDate) = CURRENT_DATE
      """)
  List<Assignment> findAssignmentsDueToday();

}
