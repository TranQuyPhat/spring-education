package com.example.springboot_education.repositories.assignment;

import com.example.springboot_education.entities.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

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


//Chưa nộp + chưa quá hạn
// @Query("""
//     SELECT a FROM Assignment a 
//     JOIN a.classField c 
//     JOIN c.classUsers cu 
//     WHERE cu.student.id = :studentId
//       AND a.dueDate >= CURRENT_DATE
//       AND NOT EXISTS (
//           SELECT s FROM Submission s 
//           WHERE s.assignment = a 
//             AND s.student.id = :studentId
//       )
//     ORDER BY a.dueDate ASC
// """)
// List<Assignment> findAssignmentsByStudentId(@Param("studentId") Integer studentId);


 @Query("SELECT a FROM Assignment a JOIN a.classField c WHERE c.teacher.id = :teacherId ORDER BY a.dueDate ASC")
    List<Assignment> findAssignmentsByTeacherId(@Param("teacherId") Integer teacherId);

}

 
