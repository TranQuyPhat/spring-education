package com.example.springboot_education.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.springboot_education.entities.ClassSchedule;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Integer> {

    @Query("SELECT cs FROM ClassSchedule cs JOIN FETCH cs.classEntity")
    List<ClassSchedule> findAllWithClass();

}
