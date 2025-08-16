package com.example.springboot_education.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.springboot_education.entities.ActivityLog;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {
     @Query("SELECT al FROM ActivityLog al JOIN FETCH al.user ORDER BY al.createdAt DESC")
    List<ActivityLog> findAllWithUser();
    
    List<ActivityLog> findByUserId(Integer userId);
    List<ActivityLog> findByActionType(String actionType);
}
