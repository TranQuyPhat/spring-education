package com.example.springboot_education.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // Lấy tất cả log
    @GetMapping
    public List<ActivityLogResponseDTO> getAllLogs() {
        return service.getAllLogs();
    }

    // Tạo log mới
    @PostMapping
    public ResponseEntity<String> createLog(@RequestBody ActivityLogCreateDTO dto) {
        service.log(dto);
        return ResponseEntity.ok("Log created successfully");
    }

    // Xóa 1 log
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLog(@PathVariable Integer id) {
        service.deleteLogs(List.of(id));
        return ResponseEntity.ok("Log deleted successfully");
    }

    // Xóa nhiều log cùng lúc
    @DeleteMapping
    public ResponseEntity<String> deleteLogs(@RequestBody List<Integer> ids) {
        service.deleteLogs(ids);
        return ResponseEntity.ok("Logs deleted successfully");
    }

    // Lấy danh sách actionType CRUD
    @GetMapping("/action-types")
    public List<String> getActionTypes() {
        return service.getActionTypes();
    }
}
