package com.example.springboot_education.services;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO;
import com.example.springboot_education.dtos.activitylogs.ActivityLogResponseDTO;
import com.example.springboot_education.entities.ActivityLog;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.repositories.ActivityLogRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;
    private final UsersJpaRepository usersRepository;

    public ActivityLogService(
            ActivityLogRepository repository,
            UsersJpaRepository usersRepository) {
        this.repository = repository;
        this.usersRepository = usersRepository;
    }

    // Lấy tất cả log kèm thông tin user
    public List<ActivityLogResponseDTO> getAllLogs() {
        return repository.findAllWithUser().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Tạo log mới
    public void log(ActivityLogCreateDTO dto) {
        ActivityLog log = new ActivityLog();
        log.setActionType(dto.getActionType());
        log.setTargetId(dto.getTargetId());
        log.setTargetTable(dto.getTargetTable());
        log.setDescription(dto.getDescription());
        log.setCreatedAt(Instant.now());

        @SuppressWarnings("UnnecessaryUnboxing")
        Users user = usersRepository.findById(dto.getUserId().intValue())
                .orElseThrow(
                        () -> new HttpException("User not found with id: " + dto.getUserId(), HttpStatus.NOT_FOUND));

        log.setUser(user);

        repository.save(log);
    }

    public void deleteLogs(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new HttpException("No IDs provided for deletion", HttpStatus.BAD_REQUEST);
        }

        List<Integer> notFoundIds = ids.stream()
                .filter(id -> !repository.existsById(id))
                .toList();

        if (!notFoundIds.isEmpty()) {
            throw new HttpException("Logs not found with ids: " + notFoundIds, HttpStatus.NOT_FOUND);
        }

        repository.deleteAllById(ids);
    }

    // Lấy danh sách actionType CRUD
    public List<String> getActionTypes() {
        return List.of("CREATE", "READ", "UPDATE", "DELETE");
    }

    @SuppressWarnings("UnnecessaryUnboxing")
    private ActivityLogResponseDTO toDTO(ActivityLog activity) {
        ActivityLogResponseDTO dto = new ActivityLogResponseDTO();
        dto.setId(activity.getId());
        dto.setActionType(activity.getActionType());
        dto.setTargetId(activity.getTargetId());
        dto.setTargetTable(activity.getTargetTable());
        dto.setDescription(activity.getDescription());
        dto.setCreatedAt(activity.getCreatedAt());

        if (activity.getUser() != null) {
            dto.setUserId(activity.getUser().getId().intValue());
            dto.setFullName(activity.getUser().getFullName());
        } else {
            dto.setUserId(null);
            dto.setFullName("N/A");
        }

        dto.setClassId(null);

        return dto;
    }
}
