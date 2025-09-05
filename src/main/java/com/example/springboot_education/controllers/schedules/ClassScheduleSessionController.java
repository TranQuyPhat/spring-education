package com.example.springboot_education.controllers.schedules;
import java.util.List;


import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot_education.dtos.classschedules.ClassScheduleSessionCreateDTO;
import com.example.springboot_education.dtos.classschedules.ClassScheduleSessionResponseDTO;
import com.example.springboot_education.dtos.classschedules.SessionLocationUpdateDTO;
import com.example.springboot_education.dtos.classschedules.SessionStatusUpdateDTO;
import com.example.springboot_education.entities.ClassScheduleSession;
import com.example.springboot_education.services.schedules.ClassScheduleSessionService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;



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
    @GetMapping("/{id}")
    public ResponseEntity<ClassScheduleSessionResponseDTO> getSessionById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }
    @PatchMapping("/{id}/location")
    public ClassScheduleSessionResponseDTO updateLocation(@PathVariable("id") Integer id,
                                                        @RequestBody SessionLocationUpdateDTO dto) {
        return sessionService.updateLocation(id, dto);
    }


    @PatchMapping("/{id}/status")
    public ClassScheduleSessionResponseDTO updateStatus(@PathVariable("id") Integer id,
                                                        @RequestBody SessionStatusUpdateDTO dto) {
        return sessionService.updateStatus(id, dto);
    }

    @PostMapping
    public ClassScheduleSessionResponseDTO create(@RequestBody ClassScheduleSessionCreateDTO dto) {
        return sessionService.create(dto);
    }

}
