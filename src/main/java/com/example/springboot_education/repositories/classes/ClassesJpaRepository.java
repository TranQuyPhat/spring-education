package com.example.springboot_education.repositories.classes;

import com.example.springboot_education.entities.ClassEntity;


import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassesJpaRepository extends JpaRepository<ClassEntity, Integer> {
}
