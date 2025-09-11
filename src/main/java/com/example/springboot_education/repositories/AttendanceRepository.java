package com.example.springboot_education.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.example.springboot_education.entities.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    List<Attendance> findBySession_Id(Integer sessionId);
    Optional<Attendance> findBySessionIdAndStudentId(Integer sessionId, Integer studentId);
    List<Attendance> findByStudent_IdAndSession_ClassEntity_Id(Integer studentId, Integer classId);

}