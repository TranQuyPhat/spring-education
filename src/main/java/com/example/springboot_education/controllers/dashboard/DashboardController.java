package com.example.springboot_education.controllers.dashboard;


import com.example.springboot_education.dtos.classDTOs.ClassAggregateDTO;
import com.example.springboot_education.dtos.dashboard.TeacherDashboardSummaryDTO;
import com.example.springboot_education.services.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers/{teacherId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<TeacherDashboardSummaryDTO> getSummary(
            @PathVariable Integer teacherId,
            @RequestParam(required = false) Integer schoolYear,
            @RequestParam(required = false) String semester
    ) {
        TeacherDashboardSummaryDTO result = dashboardService.getTeacherSummary(teacherId, schoolYear, semester);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/classes/summary")
    public ResponseEntity<List<ClassAggregateDTO>> getClassStats(
            @PathVariable Integer teacherId,
            @RequestParam(required = false) Integer schoolYear,
            @RequestParam(required = false) String semester
    ) {
        List<ClassAggregateDTO> results = dashboardService.getClassBreakdown(teacherId, schoolYear, semester);
        return ResponseEntity.ok(results);
    }
}