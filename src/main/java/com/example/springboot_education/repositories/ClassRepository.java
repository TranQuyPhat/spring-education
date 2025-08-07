package com.example.springboot_education.repositories;

import com.example.springboot_education.entities.ClassEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Integer> {
    List<ClassEntity> findByTeacher_Id(Integer teacherId);
    Page<ClassEntity> findByTeacher_Id(Integer teacherId, Pageable pageable);
}