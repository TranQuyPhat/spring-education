package com.example.springboot_education.dtos.dashboard;

public record TeacherDashboardSummaryDTO(
        Integer teacherId,
        Long totalClasses,
        Long totalStudents,
        Long totalAssignments,
        Long totalDocuments,
        Long totalSubmissions
) {}