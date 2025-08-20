package com.example.springboot_education.repositories;



import org.springframework.data.jpa.repository.JpaRepository;

import com.example.springboot_education.entities.ClassJoinRequest;

import java.util.List;

public interface ClassJoinRequestRepository extends JpaRepository<ClassJoinRequest, Integer> {
    List<ClassJoinRequest> findByClassEntity_IdAndStatus(Integer classId, ClassJoinRequest.Status status);
    List<ClassJoinRequest> findByClassEntity_Id(Integer classId);
    List<ClassJoinRequest> findByStudentId(Integer studentId);
    boolean existsByClassEntity_IdAndStudent_IdAndStatus(
        Integer classId,
        Integer studentId,
        ClassJoinRequest.Status status
    );
    List<ClassJoinRequest> findByClassEntity_Teacher_Id(Integer teacherId);

    List<ClassJoinRequest> findByClassEntity_Teacher_IdAndStatus(Integer teacherId, ClassJoinRequest.Status status);
}