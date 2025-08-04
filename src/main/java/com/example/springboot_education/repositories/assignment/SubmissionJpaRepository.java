package com.example.springboot_education.repositories.assignment;

import com.example.springboot_education.entities.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionJpaRepository extends JpaRepository<Submission, Integer> {
    List<Submission> findByAssignmentId(Integer assignmentId);
    List<Submission> findByStudentId(Integer studentId);
    Optional<Submission> findByAssignmentIdAndStudentId(Integer assignmentId, Integer studentId);
}
