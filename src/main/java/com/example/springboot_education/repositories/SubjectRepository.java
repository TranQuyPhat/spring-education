package com.example.springboot_education.repositories;


import com.example.springboot_education.entities.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    @Query("""
      SELECT s.id, s.subjectName
      FROM Subject s
    """)
    List<Object[]> findAllSubjectsIdName();
    boolean existsBySubjectName(String subjectName);

}