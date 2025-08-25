package com.example.springboot_education.dtos.dashboardsClient;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import com.example.springboot_education.dtos.assignmentDTOs.UpcomingAssignmentDto;

import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {
    private int enrolledClasses;
    private int totalAssignments;
    private int completedAssignments;
  private List<UpcomingAssignmentDto> upcomingDeadlines;
   @Builder.Default
    private List<ClassProgressDTO> classProgress = new ArrayList<>();
}