package com.example.springboot_education.dtos.dashboardsClient;

import com.example.springboot_education.dtos.GradeDistributionDTO;
import com.example.springboot_education.dtos.assignmentDTOs.UpcomingSubmissionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
    public class TeacherDashboardResponse {
        private int totalClasses;
        private int totalStudents;
        private int totalAssignments;
        private int pendingGrading;
        private List<DashboardActivityDTO> recentActivities;
        private List<UpcomingSubmissionDto> upcomingDeadlinesTeacher;
    private GradeDistributionDTO gradeDistribution;
    }