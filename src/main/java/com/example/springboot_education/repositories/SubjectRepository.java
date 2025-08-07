package com.example.springboot_education.repositories;


import com.example.springboot_education.entities.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {
}