package com.example.springboot_education.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO;
import com.example.springboot_education.dtos.classschedules.ClassScheduleCreateDTO;
import com.example.springboot_education.dtos.classschedules.ClassScheduleResponseDTO;
import com.example.springboot_education.dtos.classschedules.ClassScheduleUpdateDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.ClassSchedule;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.ClassScheduleRepository;

@Service
public class ClassScheduleService {

    private final ClassScheduleRepository repository;
    private final ClassRepository classRepository;
    private final ActivityLogService activityLogService;

    public ClassScheduleService(ClassScheduleRepository repository, ClassRepository classRepository, ActivityLogService activityLogService) {
        this.repository = repository;
        this.classRepository = classRepository;
        this.activityLogService = activityLogService;
    }

    public ClassScheduleResponseDTO create(ClassScheduleCreateDTO dto) {
        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        ClassSchedule schedule = new ClassSchedule();
        schedule.setClassEntity(classEntity);
        schedule.setDayOfWeek(dto.getDayOfWeek());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setLocation(dto.getLocation());

        ClassSchedule saved = repository.save(schedule);

        // Ghi log CREATE
        ActivityLogCreateDTO log = new ActivityLogCreateDTO();
        log.setActionType("CREATE");
        log.setTargetId(saved.getId());
        log.setTargetTable("class_schedules");
        log.setDescription("Tạo lịch trình mới " );
        log.setClassId(saved.getClassEntity().getId());
        log.setUserId(saved.getClassEntity().getTeacher().getId());
        activityLogService.log(log);

        return mapToDTO(saved);
    }

    public List<ClassScheduleResponseDTO> getAll() {
        return repository.findAllWithClass()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public org.springframework.data.domain.Page<ClassScheduleResponseDTO> getAll(org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::mapToDTO);
    }

    public ClassScheduleResponseDTO update(Integer id, ClassScheduleUpdateDTO dto) {
        ClassSchedule schedule = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class Schedule not found"));

        schedule.setDayOfWeek(dto.getDayOfWeek());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setLocation(dto.getLocation());

        ClassSchedule updated = repository.save(schedule);

        // Ghi log UPDATE
        ActivityLogCreateDTO log = new ActivityLogCreateDTO();
        log.setActionType("UPDATE");
        log.setTargetId(updated.getId());
        log.setTargetTable("class_schedules");
        log.setDescription("Cập nhật lịch trình ");
        log.setClassId(updated.getClassEntity().getId());
        log.setUserId(updated.getClassEntity().getTeacher().getId());
        activityLogService.log(log);

        return mapToDTO(updated);
    }

    private ClassScheduleResponseDTO mapToDTO(ClassSchedule schedule) {
        ClassScheduleResponseDTO dto = new ClassScheduleResponseDTO();
        dto.setId(schedule.getId());
        dto.setClassId(schedule.getClassEntity().getId());
        dto.setClassName(schedule.getClassEntity().getClassName());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setLocation(schedule.getLocation());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setUpdatedAt(schedule.getUpdatedAt());
        return dto;
    }
}
