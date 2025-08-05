package com.example.springboot_education.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot_education.dtos.attendances.AttendanceCreateDTO;
import com.example.springboot_education.dtos.attendances.AttendanceResponseDTO;
import com.example.springboot_education.dtos.attendances.AttendanceUpdateDTO;
import com.example.springboot_education.services.AttendanceService;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @GetMapping("/class-schedule/{scheduleId}")
    public List<AttendanceResponseDTO> getByClassSchedule(@PathVariable("scheduleId") Integer scheduleId) {
        return service.getByScheduleId(scheduleId);
    }

    @PostMapping
    public AttendanceResponseDTO create(@RequestBody AttendanceCreateDTO dto) {
        return service.create(dto);
    }

    @PatchMapping("/{id}")
    public AttendanceResponseDTO updateStatus(@PathVariable("id") Integer id, @RequestBody AttendanceUpdateDTO dto) {
        return service.updateStatus(id, dto);
    }

}
