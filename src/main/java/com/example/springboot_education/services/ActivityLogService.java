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
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ActivityLogResponseDTO mapToDTO(ActivityLog log) {
        ActivityLogResponseDTO dto = new ActivityLogResponseDTO();
        dto.setId(log.getId());

        // Nếu ActivityLog entity có @ManyToOne Users user:
        dto.setUserId(log.getUser().getId());
        dto.setActionType(log.getActionType());
        dto.setTargetTable(log.getTargetTable());
        dto.setTargetId(log.getTargetId());
        dto.setDescription(log.getDescription());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
