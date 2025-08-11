package com.example.springboot_education.services;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO;
import com.example.springboot_education.dtos.activitylogs.ActivityLogResponseDTO;
import com.example.springboot_education.entities.ActivityLog;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.ActivityLogRepository;
import com.example.springboot_education.repositories.ClassJpaRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;
    private final UsersJpaRepository usersRepository;

    public ActivityLogService(
            ActivityLogRepository repository,
            ClassJpaRepository classRepository,
            UsersJpaRepository usersRepository
    ) {
        this.repository = repository;
        this.usersRepository = usersRepository;
    }

    public List<ActivityLogResponseDTO> getAllLogs() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void log(ActivityLogCreateDTO dto) {
        ActivityLog log = new ActivityLog();
        log.setActionType(dto.getActionType());
        log.setTargetId(dto.getTargetId());
        log.setTargetTable(dto.getTargetTable());
        log.setDescription(dto.getDescription());
        log.setCreatedAt(Instant.now());

        // Find and set user
        Users user = usersRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));
        log.setUser(user);

        repository.save(log);
    }

    private ActivityLogResponseDTO toDTO(ActivityLog activity) {
        ActivityLogResponseDTO dto = new ActivityLogResponseDTO();
        dto.setId(activity.getId());
        dto.setActionType(activity.getActionType());
        dto.setTargetId(activity.getTargetId());
        dto.setTargetTable(activity.getTargetTable());
        dto.setDescription(activity.getDescription());
        dto.setCreatedAt(activity.getCreatedAt());
        dto.setUserId(activity.getUser().getId());
        return dto;
    }
}
