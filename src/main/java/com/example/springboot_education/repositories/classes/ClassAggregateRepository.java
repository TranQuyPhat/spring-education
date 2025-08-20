package com.example.springboot_education.repositories.classes;


import com.example.springboot_education.dtos.classDTOs.ClassAggregateDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.springboot_education.entities.ClassEntity;

import java.util.List;

@Repository
public interface ClassAggregateRepository extends JpaRepository<ClassEntity, Long> {

    @Query("""
    SELECT new com.example.springboot_education.dtos.classDTOs.ClassAggregateDTO(
        c.id,
        c.className,
        COUNT(DISTINCT cu.id.studentId),
        COUNT(DISTINCT a.id),
        COUNT(DISTINCT m.id),
        COUNT(DISTINCT s.id)
    )
    FROM ClassEntity c
    LEFT JOIN ClassUser cu ON cu.classField = c
    LEFT JOIN Assignment a ON a.classField = c
    LEFT JOIN ClassMaterial m ON m.classField = c
    LEFT JOIN Submission s ON s.assignment.classField = c
    WHERE c.teacher.id = :teacherId
      AND (:schoolYear IS NULL OR c.schoolYear = :schoolYear)
      AND (:semester IS NULL OR c.semester = :semester)
    GROUP BY c.id, c.className
""")
    List<ClassAggregateDTO> aggregatePerClass(
            @Param("teacherId") Integer teacherId,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

}