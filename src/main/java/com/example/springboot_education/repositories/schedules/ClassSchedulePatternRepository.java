package com.example.springboot_education.repositories.schedules;


import com.example.springboot_education.entities.ClassSchedulePattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassSchedulePatternRepository extends JpaRepository<ClassSchedulePattern, Integer> {
    List<ClassSchedulePattern> findByClassEntity_Id(Integer classId);
}
