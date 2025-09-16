package com.example.springboot_education.repositories.classes;

import com.example.springboot_education.entities.ClassEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassesJpaRepository extends JpaRepository<ClassEntity, Integer> {

    @Query("""
                SELECT c.teacher.email
                FROM ClassEntity c
                WHERE c.id = :classId
            """)
    String findTeacherEmailByClassId(@Param("classId") Integer classId);

    @Query("""
                SELECT c.teacher.fullName
                FROM ClassEntity c
                WHERE c.id = :classId
            """)
    String findTeacherNameByClassId(@Param("classId") Integer classId);
}
