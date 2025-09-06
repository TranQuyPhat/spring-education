package com.example.springboot_education.services.schedules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.classschedules.ClassScheduleSessionCreateDTO;
import com.example.springboot_education.dtos.classschedules.ClassScheduleSessionResponseDTO;
import com.example.springboot_education.dtos.classschedules.ClassScheduleSessionUpdateDTO;
import com.example.springboot_education.dtos.classschedules.SessionLocationUpdateDTO;
import com.example.springboot_education.dtos.classschedules.SessionStatusUpdateDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.ClassSchedulePattern;
import com.example.springboot_education.entities.ClassScheduleSession;
import com.example.springboot_education.entities.Location;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.repositories.classes.ClassesJpaRepository;
import com.example.springboot_education.repositories.schedules.ClassSchedulePatternRepository;
import com.example.springboot_education.repositories.schedules.ClassScheduleSessionRepository;
import com.example.springboot_education.repositories.schedules.LocationRepository;

import jakarta.persistence.criteria.CriteriaBuilder.In;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassScheduleSessionService {
    private final ClassScheduleSessionRepository sessionRepository;
    private final ClassSchedulePatternRepository patternRepository;
    private final ClassesJpaRepository classRepository;
    private final LocationRepository locationRepository;


    @LoggableAction(value = "CREATE", entity = "class_schedule_sessions", description = "Tạo buổi học")
    public ClassScheduleSessionResponseDTO create(ClassScheduleSessionCreateDTO dto) {
        ClassSchedulePattern pattern = patternRepository.findById(dto.getPatternId())
                .orElseThrow(() -> new EntityNotFoundException("Pattern"));
        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class"));

        ClassScheduleSession session = new ClassScheduleSession();
        session.setPattern(pattern);
        session.setClassEntity(classEntity);
        session.setSessionDate(dto.getSessionDate());
        session.setStartPeriod(dto.getStartPeriod());
        session.setEndPeriod(dto.getEndPeriod());   
        session.setLocation(dto.getLocation());
        session.setStatus(ClassScheduleSession.SessionStatus.valueOf(dto.getStatus()));
        session.setNote(dto.getNote());

        return mapToDTO(sessionRepository.save(session));
    }
    public ClassScheduleSessionResponseDTO getSessionById(Integer sessionId) {
        ClassScheduleSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + sessionId));
        return mapToDTO(session);
    }

    public List<ClassScheduleSessionResponseDTO> getAllByClass(Integer classId) {
        return sessionRepository.findByClassEntity_Id(classId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ClassScheduleSessionResponseDTO> getAllByPattern(Integer patternId) {
        return sessionRepository.findByPattern_Id(patternId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @LoggableAction(value = "UPDATE", entity = "class_schedule_sessions", description = "Cập nhật buổi học")
    public ClassScheduleSessionResponseDTO update(Integer id, ClassScheduleSessionUpdateDTO dto) {
        ClassScheduleSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Session"));

        session.setSessionDate(dto.getSessionDate());
        session.setStartPeriod(dto.getStartPeriod());
        session.setEndPeriod(dto.getEndPeriod());
        session.setLocation(dto.getLocation());
        session.setStatus(ClassScheduleSession.SessionStatus.valueOf(dto.getStatus()));
        session.setNote(dto.getNote());

        return mapToDTO(sessionRepository.save(session));
    }

    public void delete(Integer id) {
        if (!sessionRepository.existsById(id)) {
            throw new EntityNotFoundException("Session");
        }
        sessionRepository.deleteById(id);
    }

    public ClassScheduleSessionResponseDTO updateStatus(Integer id, SessionStatusUpdateDTO dto) {
        ClassScheduleSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setStatus(ClassScheduleSession.SessionStatus.valueOf(dto.getStatus()));
        return mapToDTO(sessionRepository.save(session));
    }

    public ClassScheduleSessionResponseDTO updateLocation(Integer id, SessionLocationUpdateDTO dto) {
        ClassScheduleSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // lấy entity Location mới
        // Location location = locationRepository.findById(dto.getLocationId())
        //         .orElseThrow(() -> new RuntimeException("Location not found: " + dto.getLocationId()));

        session.setLocation(dto.getLocation()); // nếu ClassScheduleSession có quan hệ @ManyToOne Location
        return mapToDTO(sessionRepository.save(session));
    }

    private ClassScheduleSessionResponseDTO mapToDTO(ClassScheduleSession entity) {
        ClassScheduleSessionResponseDTO dto = new ClassScheduleSessionResponseDTO();
        dto.setId(entity.getId());
        dto.setPatternId(entity.getPattern().getId());
        dto.setClassId(entity.getClassEntity().getId());
        dto.setSessionDate(entity.getSessionDate());
        dto.setStartPeriod(entity.getStartPeriod());
        dto.setEndPeriod(entity.getEndPeriod());
        dto.setLocation(entity.getLocation());
        dto.setStatus(entity.getStatus().name());
        dto.setNote(entity.getNote());
        return dto;
    }
}
