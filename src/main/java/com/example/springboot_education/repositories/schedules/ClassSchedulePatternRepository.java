package com.example.springboot_education.repositories.schedules;


import com.example.springboot_education.entities.ClassEntity;

import com.example.springboot_education.entities.ClassSchedulePattern;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassSchedulePatternRepository extends JpaRepository<ClassSchedulePattern, Integer> {
    List<ClassSchedulePattern> findByClassEntity_Id(Integer classId);
    boolean existsByClassEntity(ClassEntity classEntity);

    boolean existsByLocationId(Integer locationId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ClassScheduleSession s WHERE s.pattern.id = :patternId")
    void deleteByPatternId(@Param("patternId") Integer patternId);
}
