package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.activitylogs.ActivityLogResponseDTO;

import com.example.springboot_education.services.ActivityLogService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
}
