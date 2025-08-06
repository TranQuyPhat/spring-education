package com.example.springboot_education.services;

import com.example.springboot_education.dtos.activitylogs.ActivityLogResponseDTO;
import com.example.springboot_education.entities.ActivityLog;
import com.example.springboot_education.repositories.ActivityLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public ActivityLogService(ActivityLogRepository repository) {
        this.repository = repository;
    }

    public List<ActivityLogResponseDTO> getAllLogs() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ActivityLogResponseDTO toDTO(ActivityLog activity) {
        ActivityLogResponseDTO dto = new ActivityLogResponseDTO();
        dto.setId(activity.getId());
        dto.setActionType(activity.getActionType());
        dto.setTargetId(activity.getTargetId());
        dto.setTargetTable(activity.getTargetTable());
        dto.setDescription(activity.getDescription());
        dto.setCreatedAt(activity.getCreatedAt());
        dto.setClassId(activity.getClassRoom().getId());
        dto.setUserId(activity.getUser().getId());
        return dto;
    }
}
