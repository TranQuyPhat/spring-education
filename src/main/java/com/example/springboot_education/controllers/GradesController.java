package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.gradeDTOs.*;
import com.example.springboot_education.services.grade.GradesService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradesController {

    private final GradesService gradesService;

    @GetMapping("/overview")
    public GradesOverviewDTO getOverview(
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate
    ) {
        return gradesService.getOverview(classId, subjectId, startDate, endDate);
    }

    @GetMapping("/rankings")
    public List<StudentRankingDTO> getRankings(
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        return gradesService.getRankings(classId, subjectId, limit);
    }

    @GetMapping("/student/{studentId}/rank")
    public StudentRankPositionDTO getStudentRank(
            @PathVariable Integer studentId,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer subjectId
    ) {
        return gradesService.getStudentRank(studentId, classId, subjectId);
    }

    @GetMapping("/student/{studentId}/details")
    public StudentDetailsDTO getStudentDetails(
            @PathVariable Integer studentId
    ) {
        return gradesService.getStudentDetails(studentId);
    }

    @GetMapping("/subject-analysis")
    public List<SubjectAnalysisDTO> getSubjectAnalysis() {
        return gradesService.getSubjectAnalysis();
    }

    @GetMapping("/trends")
    public List<TrendDTO> getTrends(
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(defaultValue = "day") String timeUnit
    ) {
        return gradesService.getTrends(classId, subjectId, timeUnit);
    }

//    student
    @GetMapping("/class/{classId}/student/{studentId}")
    public List<GradeResultDTO> getResults(
            @PathVariable Integer classId,
            @PathVariable Integer studentId
    ) {
        return gradesService.getStudentResults(classId, studentId);
    }
}
