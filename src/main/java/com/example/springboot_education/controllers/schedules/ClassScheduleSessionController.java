package com.example.springboot_education.controllers.schedules;
import java.util.List;


import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import com.example.springboot_education.dtos.classschedules.ClassScheduleSessionResponseDTO;

import com.example.springboot_education.services.schedules.ClassScheduleSessionService;

import lombok.RequiredArgsConstructor;





@RestController
@RequestMapping("/api/auth/sessions")
@RequiredArgsConstructor
public class ClassScheduleSessionController {

    private final ClassScheduleSessionService sessionService;

    // Lấy danh sách buổi học theo classId
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ClassScheduleSessionResponseDTO>> getAllByClass(
            @PathVariable("classId") Integer classId) {
        List<ClassScheduleSessionResponseDTO> sessions = sessionService.getAllByClass(classId);
        return ResponseEntity.ok(sessions);
    }
}
