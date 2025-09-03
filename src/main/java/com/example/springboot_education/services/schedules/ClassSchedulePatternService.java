package com.example.springboot_education.services.schedules;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.classschedules.ClassSchedulePatternCreateDTO;
import com.example.springboot_education.dtos.classschedules.ClassSchedulePatternResponseDTO;
import com.example.springboot_education.dtos.classschedules.ClassSchedulePatternUpdateDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.Location;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.entities.ClassSchedulePattern;
import com.example.springboot_education.entities.ClassScheduleSession;
import com.example.springboot_education.repositories.classes.ClassesJpaRepository;
import com.example.springboot_education.repositories.schedules.ClassSchedulePatternRepository;
import com.example.springboot_education.repositories.schedules.ClassScheduleSessionRepository;
import com.example.springboot_education.repositories.schedules.LocationRepository;

import jakarta.transaction.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassSchedulePatternService {
    private final ClassSchedulePatternRepository patternRepository;
    private final ClassesJpaRepository classRepository;
    private final LocationRepository locationRepository;
    private final ClassScheduleSessionRepository sessionRepository;

    // @LoggableAction(value = "CREATE", entity = "class_schedules", description =
    // "Tạo nhiều pattern cho 1 lớp")
    public List<ClassSchedulePatternResponseDTO> createBatch(ClassSchedulePatternCreateDTO dto) {
        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class"));

        boolean exists = patternRepository.existsByClassEntity(classEntity);
        if (exists) {
            throw new RuntimeException("Class already has a schedule");
        }

        List<ClassSchedulePatternResponseDTO> result = new ArrayList<>();

        for (ClassSchedulePatternCreateDTO.SlotDTO slot : dto.getSlots()) {
            Location location = locationRepository.findById(slot.getLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Location" + slot.getLocationId()));

            // Tạo pattern
            ClassSchedulePattern pattern = new ClassSchedulePattern();
            pattern.setClassEntity(classEntity);
            pattern.setDayOfWeek(DayOfWeek.valueOf(slot.getDayOfWeek().toUpperCase()));
            pattern.setStartPeriod(slot.getStartPeriod());
            pattern.setEndPeriod(slot.getEndPeriod());
            pattern.setStartDate(dto.getStartDate());
            pattern.setEndDate(dto.getEndDate());
            pattern.setLocation(location);

            pattern = patternRepository.save(pattern);

            // Generate các session dựa theo pattern
            generateSessions(pattern, dto.getStartDate(), dto.getEndDate());

            result.add(mapToDTO(pattern));
        }

        return result;
    }

    private void generateSessions(ClassSchedulePattern pattern, LocalDate startDate, LocalDate endDate) {
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            if (current.getDayOfWeek().equals(pattern.getDayOfWeek())) {
                ClassScheduleSession session = new ClassScheduleSession();
                session.setPattern(pattern);
                session.setClassEntity(pattern.getClassEntity());
                session.setSessionDate(current);
                session.setStartPeriod(pattern.getStartPeriod());
                session.setEndPeriod(pattern.getEndPeriod());
                session.setLocation(pattern.getLocation().getRoomName());
                session.setStatus(ClassScheduleSession.SessionStatus.SCHEDULED);

                sessionRepository.save(session);
            }
            current = current.plusDays(1);
        }
    }

    public List<ClassSchedulePatternResponseDTO> getAllByClass(Integer classId) {
        classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class"));

        return patternRepository.findByClassEntity_Id(classId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<ClassSchedulePatternResponseDTO> getAll(Pageable pageable) {
        return patternRepository.findAll(pageable).map(this::mapToDTO);
    }

    public List<ClassSchedulePatternResponseDTO> updateBatch(ClassSchedulePatternUpdateDTO dto) {
        List<ClassSchedulePatternResponseDTO> result = new ArrayList<>();

        for (ClassSchedulePatternUpdateDTO.PatternUpdateDTO item : dto.getPatterns()) {
            ClassSchedulePattern pattern = patternRepository.findById(item.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Pattern" + item.getId()));

            Location location = locationRepository.findById(item.getLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Location" + item.getLocationId()));

            if (pattern.getClassEntity() == null ||
                    !classRepository.existsById(pattern.getClassEntity().getId())) {
                throw new EntityNotFoundException(
                        "ClassEntity for pattern " + item.getId());
            }

            if (item.getDayOfWeek() != null) {
                pattern.setDayOfWeek(DayOfWeek.valueOf(item.getDayOfWeek().toUpperCase()));
            }
            pattern.setStartPeriod(item.getStartPeriod());
            pattern.setEndPeriod(item.getEndPeriod());
            pattern.setStartDate(item.getStartDate());
            pattern.setEndDate(item.getEndDate());
            pattern.setLocation(location);

            pattern = patternRepository.save(pattern);

            // Xoá session cũ
            patternRepository.deleteByPatternId(pattern.getId());

            // Generate session mới
            generateSessions(pattern, pattern.getStartDate(), pattern.getEndDate());

            result.add(mapToDTO(pattern));
        }

        return result;
    }

    public void delete(Integer id) {
        ClassSchedulePattern pattern = patternRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pattern"));

        // Xoá tất cả session gắn với pattern
        patternRepository.deleteByPatternId(pattern.getId());

        // Xoá pattern
        patternRepository.delete(pattern);
    }

    private ClassSchedulePatternResponseDTO mapToDTO(ClassSchedulePattern entity) {
        ClassSchedulePatternResponseDTO dto = new ClassSchedulePatternResponseDTO();
        dto.setId(entity.getId());
        dto.setClassId(entity.getClassEntity().getId());
        dto.setClassName(entity.getClassEntity().getClassName());
        dto.setDayOfWeek(entity.getDayOfWeek().name()); // convert enum -> String
        dto.setStartPeriod(entity.getStartPeriod());
        dto.setEndPeriod(entity.getEndPeriod());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setLocation(entity.getLocation().getRoomName());
        return dto;
    }
}
