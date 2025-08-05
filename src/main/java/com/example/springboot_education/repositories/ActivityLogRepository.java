package com.example.springboot_education.repositories;

import com.example.springboot_education.entities.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {
    List<ActivityLog> findByUserId(Integer userId);
    List<ActivityLog> findByActionType(String actionType);
}
