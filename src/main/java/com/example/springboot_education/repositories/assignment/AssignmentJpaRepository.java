package com.example.springboot_education.repositories.assignment;

import com.example.springboot_education.entities.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentJpaRepository extends JpaRepository<Assignment, Integer> {
    List<Assignment> findByClassField_Id(Integer classId);
}
