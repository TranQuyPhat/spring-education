package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.gradeDTOs.*;
import com.example.springboot_education.services.grade.StudentGradesService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/student/grades")
@RequiredArgsConstructor
public class StudentGradesController {

    private final StudentGradesService service;

    // 1. Overview
    @GetMapping("/overview")
    public StudentOverviewDTO overview(
            @RequestParam Integer studentId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Convert LocalDate → LocalDateTime
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        return service.getOverview(studentId, subjectId, startDateTime, endDateTime);
    }


    // 2. By-subject
    @GetMapping("/by-subject")
    public List<SubjectAnalysisDTO> bySubject(@RequestParam Integer studentId) {
        return service.getBySubject(studentId);
    }

    // 3. Details
    @GetMapping("/details")
    public StudentDetailsDTO details(
            @RequestParam Integer studentId,
            @RequestParam(required = false) Integer subjectId
    ) {
        return service.getDetails(studentId, subjectId);
    }

    // 4. Trends
    @GetMapping("/trends")
    public List<TrendDTO> trends(
            @RequestParam Integer studentId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(defaultValue = "day") String timeUnit
    ) {
        return service.getTrends(studentId, subjectId, timeUnit);
    }

    // 5. Compare with class
    @GetMapping("/compare")
    public CompareDto compare(
            @RequestParam Integer studentId,
            @RequestParam Integer classId,
            @RequestParam(required = false) Integer subjectId
    ) {
        return service.compareWithClass(studentId, classId, subjectId);
    }
}
