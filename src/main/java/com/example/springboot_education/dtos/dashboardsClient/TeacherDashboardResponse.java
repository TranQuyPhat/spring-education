package com.example.springboot_education.dtos.dashboardsClient;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.springboot_education.dtos.assignmentDTOs.UpcomingSubmissionDto;

import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDashboardResponse {
    private int totalClasses;
    private int totalStudents;
    private int totalAssignments;
    private int pendingGrading;
    private double averageGrade;
    private List<DashboardActivityDTO> recentActivities;
    private List<UpcomingSubmissionDto> upcomingDeadlinesTeacher;
}