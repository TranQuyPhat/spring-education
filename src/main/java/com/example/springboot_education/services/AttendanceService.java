package com.example.springboot_education.services;

import com.example.springboot_education.dtos.attendances.AttendanceRequestDTO;
import com.example.springboot_education.dtos.attendances.AttendanceResponseDTO;
import com.example.springboot_education.dtos.attendances.BulkAttendanceRequestDTO;
import com.example.springboot_education.entities.Attendance;
import com.example.springboot_education.entities.ClassScheduleSession;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.entities.ClassScheduleSession.SessionStatus;
import com.example.springboot_education.repositories.AttendanceRepository;
import com.example.springboot_education.repositories.schedules.ClassScheduleSessionRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ClassScheduleSessionRepository sessionRepository;
    private final UsersJpaRepository usersRepository;

    // public void recordAttendance(Integer sessionId, List<AttendanceRequestDTO> records) {
    //     ClassScheduleSession session = sessionRepository.findById(sessionId)
    //             .orElseThrow(() -> new RuntimeException("Session not found"));

    //     Instant submitTime = Instant.now();
    //     session.setSubmittedAt(submitTime); // nếu bạn đã thêm submittedAt trong session entity

    //     for (AttendanceRequestDTO dto : records) {
    //         Users student = usersRepository.findById(dto.getStudentId())
    //                 .orElseThrow(() -> new RuntimeException("Student not found"));

    //         Attendance record = new Attendance();
    //         record.setSession(session);
    //         record.setStudent(student);
    //         record.setStatus(dto.getStatus());
    //         record.setNote(dto.getNote());
    //         record.setMarkedAt(submitTime);

    //         attendanceRepository.save(record);
    //     }
    // }


    @Transactional
    public void recordAttendance(Integer sessionId, BulkAttendanceRequestDTO request) {
        ClassScheduleSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Instant submitTime = Instant.now();
        session.setSubmittedAt(submitTime);
        session.setStatus(SessionStatus.COMPLETED);

        // cập nhật note buổi học
        if (request.getNoteSession() != null) {
            session.setNote(request.getNoteSession());
        }


        for (AttendanceRequestDTO dto : request.getRecords()) {
            Users student = usersRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            Attendance record = attendanceRepository
                    .findBySessionIdAndStudentId(sessionId, dto.getStudentId())
                    .orElse(new Attendance());

            record.setSession(session);
            record.setStudent(student);
            record.setStatus(dto.getStatus());
            record.setNote(dto.getNote());
            record.setMarkedAt(submitTime);

            attendanceRepository.save(record);
        }

        sessionRepository.save(session);
    }

    public List<AttendanceResponseDTO> getAttendance(Integer sessionId) {
        List<Attendance> records = attendanceRepository.findBySession_Id(sessionId);
        return records.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public AttendanceResponseDTO updateAttendance(Integer recordId, AttendanceRequestDTO dto) {
        Attendance record = attendanceRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        Users student = usersRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        record.setStudent(student);
        record.setStatus(dto.getStatus());
        record.setNote(dto.getNote());
        record.setMarkedAt(Instant.now());

        Attendance updated = attendanceRepository.save(record);
        return mapToDto(updated);
    }

    private AttendanceResponseDTO mapToDto(Attendance record) {
        AttendanceResponseDTO dto = new AttendanceResponseDTO();
        dto.setId(record.getId());
        dto.setStudentId(record.getStudent().getId());
        dto.setStudentName(record.getStudent().getFullName());
        dto.setSessionId(record.getSession().getId());
        dto.setStatus(record.getStatus());
        dto.setNote(record.getNote());
        dto.setMarkedAt(record.getMarkedAt());
        return dto;
    }
}
