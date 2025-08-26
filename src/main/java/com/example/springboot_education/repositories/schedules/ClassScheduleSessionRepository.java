package com.example.springboot_education.repositories.schedules;

import com.example.springboot_education.entities.ClassScheduleSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ClassScheduleSessionRepository  extends JpaRepository<ClassScheduleSession, Integer> {
    List<ClassScheduleSession> findByClassEntity_Id(Integer classId);
    List<ClassScheduleSession> findByPattern_Id(Integer patternId);
    List<ClassScheduleSession> findBySessionDate(LocalDate date);
    void deleteByPatternId(Integer patternId);
}
