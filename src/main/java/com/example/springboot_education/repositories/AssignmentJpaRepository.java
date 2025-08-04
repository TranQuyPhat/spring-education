package com.example.springboot_education.repositories;

import com.example.springboot_education.entities.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentJpaRepository extends JpaRepository<Assignment, Long> {
}
