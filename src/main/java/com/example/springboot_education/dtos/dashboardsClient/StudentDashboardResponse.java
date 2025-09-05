package com.example.springboot_education.dtos.dashboardsClient;

import com.example.springboot_education.dtos.assignmentDTOs.UpcomingAssignmentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {
    private int enrolledClasses;
    private int totalAssignments;
    private Double avgScore;
    private int completedAssignments;
  private List<UpcomingAssignmentDto> upcomingDeadlines;
   @Builder.Default
    private List<ClassProgressDTO> classProgress = new ArrayList<>();
}