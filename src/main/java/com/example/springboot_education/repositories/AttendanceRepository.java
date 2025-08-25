package com.example.springboot_education.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.springboot_education.entities.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    // Lấy danh sách điểm danh theo schedule_id
    @Query("SELECT a FROM Attendance a " +
            "JOIN FETCH a.student s " +
            "JOIN FETCH a.schedule sch " +
            "WHERE sch.id = :scheduleId")
    List<Attendance> findByScheduleId(@Param("scheduleId") Integer scheduleId);

    // Lấy danh sách điểm danh theo student_id
    @Query("SELECT a FROM Attendance a " +
            "JOIN FETCH a.student s " +
            "JOIN FETCH a.schedule sch " +
            "WHERE s.id = :studentId")
    List<Attendance> findByStudentId(@Param("studentId") Long studentId);

    // Lấy danh sách điểm danh theo class_id
    @Query("SELECT a FROM Attendance a " +
            "JOIN FETCH a.student s " +
            "JOIN FETCH a.schedule sch " +
            "WHERE sch.classEntity.id = :classId")
    List<Attendance> findByClassId(@Param("classId") Integer classId);

    // Lấy toàn bộ điểm danh kèm student + schedule (tránh N+1 query)
    @Query("SELECT a FROM Attendance a " +
            "JOIN FETCH a.student s " +
            "JOIN FETCH a.schedule sch")
    List<Attendance> findAllWithDetails();
}