package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.attendances.AttendanceRequestDTO;
import com.example.springboot_education.dtos.attendances.AttendanceResponseDTO;
import com.example.springboot_education.services.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Submit điểm danh cho cả lớp trong 1 buổi học
     */
    @PostMapping("/{sessionId}")
    public ResponseEntity<String> recordAttendance(
            @PathVariable("sessionId") Integer sessionId,
            @RequestBody List<AttendanceRequestDTO> records
    ) {
        attendanceService.recordAttendance(sessionId, records);
        return ResponseEntity.ok("Attendance recorded successfully");
    }

    /**
     * Lấy danh sách điểm danh theo buổi học
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<List<AttendanceResponseDTO>> getAttendance(
            @PathVariable("sessionId") Integer sessionId
    ) {
        List<AttendanceResponseDTO> records = attendanceService.getAttendance(sessionId);
        return ResponseEntity.ok(records);
    }

    /**
     * Cập nhật điểm danh cho một học sinh
     */
    @PutMapping("/{recordId}")
    public ResponseEntity<AttendanceResponseDTO> updateAttendance(
            @PathVariable Integer recordId,
            @RequestBody AttendanceRequestDTO dto
    ) {
        AttendanceResponseDTO updated = attendanceService.updateAttendance(recordId, dto);
        return ResponseEntity.ok(updated);
    }
}
