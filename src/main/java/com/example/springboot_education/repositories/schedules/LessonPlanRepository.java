package com.example.springboot_education.repositories.schedules;


import com.example.springboot_education.entities.LessonPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonPlanRepository extends JpaRepository<LessonPlan, Integer> {
    List<LessonPlan> findByClassRoom_Id(Integer classId);
    // Lấy tất cả lesson plan của một lớp theo thứ tự buổi học
    List<LessonPlan> findByClassRoom_IdOrderBySessionNumberAsc(Integer classId);
}