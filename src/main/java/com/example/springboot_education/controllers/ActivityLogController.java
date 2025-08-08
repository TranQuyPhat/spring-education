package com.example.springboot_education.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO;
import com.example.springboot_education.dtos.activitylogs.ActivityLogResponseDTO;
import com.example.springboot_education.services.ActivityLogService;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {
    private final ActivityLogService service;

    public ActivityLogController(ActivityLogService service) {
        this.service = service;
    }

    @GetMapping
    public List<ActivityLogResponseDTO> getAllLogs() {
        return service.getAllLogs();
    }
    @PostMapping
    public ResponseEntity<String> createLog(@RequestBody ActivityLogCreateDTO dto) {
        service.log(dto);
        return ResponseEntity.ok("Log created successfully");
    }
}
