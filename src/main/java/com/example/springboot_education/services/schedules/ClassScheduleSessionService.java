package com.example.springboot_education.services.schedules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.classschedules.ClassScheduleSessionCreateDTO;
import com.example.springboot_education.dtos.classschedules.ClassScheduleSessionResponseDTO;
import com.example.springboot_education.dtos.classschedules.ClassScheduleSessionUpdateDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.ClassSchedulePattern;
import com.example.springboot_education.entities.ClassScheduleSession;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.repositories.classes.ClassesJpaRepository;
import com.example.springboot_education.repositories.schedules.ClassSchedulePatternRepository;
import com.example.springboot_education.repositories.schedules.ClassScheduleSessionRepository;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassScheduleSessionService {
    private final ClassScheduleSessionRepository sessionRepository;
    private final ClassSchedulePatternRepository patternRepository;
    private final ClassesJpaRepository classRepository;

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
