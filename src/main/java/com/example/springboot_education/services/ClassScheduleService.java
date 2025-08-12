package com.example.springboot_education.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
    
// Xóa import ActivityLogCreateDTO
// import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO; 
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
    
    public ClassScheduleService(ClassScheduleRepository repository, ClassRepository classRepository) {
        this.repository = repository;
        this.classRepository = classRepository;
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