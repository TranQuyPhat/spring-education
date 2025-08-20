package com.example.springboot_education.services;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;


import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.attendances.AttendanceCreateDTO;
import com.example.springboot_education.dtos.attendances.AttendanceResponseDTO;
import com.example.springboot_education.dtos.attendances.AttendanceUpdateDTO;
import com.example.springboot_education.entities.Attendance;
import com.example.springboot_education.entities.ClassSchedule;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.AttendanceRepository;
import com.example.springboot_education.repositories.ClassScheduleRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;

@Service
public class AttendanceService {

    private final AttendanceRepository repository;
    private final UsersJpaRepository userRepository;
    private final ClassScheduleRepository scheduleRepository;

    public AttendanceService(AttendanceRepository repository, UsersJpaRepository userRepository,
                             ClassScheduleRepository scheduleRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
    }
    public AttendanceResponseDTO create(AttendanceCreateDTO dto) {
        Users student = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        ClassSchedule schedule = scheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setSchedule(schedule);
        attendance.setStatus(dto.getStatus());
        attendance.setMarkedAt(Instant.now());

        Attendance saved = repository.save(attendance);

        return mapToDTO(saved);
    }

    // Phương thức để điểm danh cả lớp
    @LoggableAction(value = "CREATE", entity = "attendances", description = "Tạo mới điểm danh cho cả lớp")
    public List<AttendanceResponseDTO> markClassAttendance(List<AttendanceCreateDTO> dtos) {
        return dtos.stream()
                .map(this::create) // Tái sử dụng phương thức create
                .collect(Collectors.toList());
    }

    public List<AttendanceResponseDTO> getByScheduleId(Integer scheduleId) {
        return repository.findByScheduleId(scheduleId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @LoggableAction(value = "UPDATE", entity = "attendances", description = "Cập nhật trạng thái điểm danh")
    public AttendanceResponseDTO updateStatus(Integer id, AttendanceUpdateDTO dto) {
        Attendance attendance = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found with id " + id));

        attendance.setStatus(dto.getStatus());
        Attendance updated = repository.save(attendance);
        return mapToDTO(updated);
    }

    private AttendanceResponseDTO mapToDTO(Attendance entity) {
        AttendanceResponseDTO dto = new AttendanceResponseDTO();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudent().getId());
        dto.setScheduleId(entity.getSchedule().getId());
        dto.setStatus(entity.getStatus());
        dto.setMarkedAt(entity.getMarkedAt());
        return dto;
    }
}