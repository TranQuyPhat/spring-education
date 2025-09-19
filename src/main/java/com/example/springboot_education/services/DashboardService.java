package com.example.springboot_education.services;

import com.example.springboot_education.dtos.GradeDistributionDTO;
import com.example.springboot_education.dtos.assignmentDTOs.UpcomingAssignmentDto;
import com.example.springboot_education.dtos.assignmentDTOs.UpcomingSubmissionDto;
import com.example.springboot_education.dtos.dashboardsClient.DashboardActivityDTO;
import com.example.springboot_education.dtos.dashboardsClient.StudentDashboardResponse;
import com.example.springboot_education.dtos.dashboardsClient.TeacherDashboardResponse;
import com.example.springboot_education.dtos.gradeDTOs.GradeBase.WeightedScorePerClassDTO;
import com.example.springboot_education.repositories.DashboardRepository;
import com.example.springboot_education.services.assignment.AssignmentService;
import com.example.springboot_education.services.grade.impl.GradeStatsServiceImpl;
import com.example.springboot_education.untils.TimeAgoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final AssignmentService assignmentService;
    private final GradeStatsServiceImpl gradeStatsService;
public TeacherDashboardResponse getTeacherDashboard(Integer teacherId) {
    List<DashboardActivityDTO> recentActivities = new ArrayList<>();
    List<UpcomingSubmissionDto> upcomingDeadlines = assignmentService.getUpcomingSubmissions(teacherId);
    // --- 1. Lấy recent submissions ---
    List<Object[]> submissions = dashboardRepository.countSubmissionsPerAssignmentByTeacher(teacherId);
    // Object[]: [assignmentId, assignmentTitle, className, countStudents, latestSubmittedAt]
  for (Object[] row : submissions) {
    DashboardActivityDTO dto = new DashboardActivityDTO();
    dto.setMessage(row[3] + " học sinh đã nộp bài tập " + row[1]);
    dto.setType("submission");
    dto.setClassName((String) row[2]);
      LocalDateTime ldt = (LocalDateTime) row[4];
      if (ldt != null) {
          Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
          dto.setTime(TimeAgoUtil.timeAgo(instant));
          dto.setSortTime(instant);
      }
    recentActivities.add(dto);
}

    // --- 2. Lấy recent comments ---
    List<Object[]> comments = dashboardRepository.findRecentCommentsByTeacher(teacherId);
    // Object[]: [commentId, studentName, assignmentTitle, className, createdAt]
for (Object[] row : comments) {
    DashboardActivityDTO dto = new DashboardActivityDTO();
    dto.setMessage(row[1] + " đã đặt câu hỏi về bài tập " + row[2]);
    dto.setType("question");
    dto.setClassName((String) row[3]);
    Timestamp ts = (Timestamp) row[4];
    if (ts != null) {
        dto.setTime(TimeAgoUtil.timeAgo(ts.toInstant()));
        dto.setSortTime(ts.toInstant());
    }
    recentActivities.add(dto);
}
    recentActivities.sort((a, b) -> b.getSortTime().compareTo(a.getSortTime()));
List<DashboardActivityDTO> top5Activities = recentActivities.stream()
        .limit(5)
        .toList();
    Object[] distribution = (Object[]) dashboardRepository.findGradeDistribution(teacherId.longValue());
    GradeDistributionDTO gradeDist = new GradeDistributionDTO(
            ((Number) distribution[0]).longValue(),
            ((Number) distribution[1]).longValue(),
            ((Number) distribution[2]).longValue(),
            ((Number) distribution[3]).longValue(),
            ((Number) distribution[4]).longValue()
    );
    return TeacherDashboardResponse.builder()
            .totalClasses((int) dashboardRepository.countClassesByTeacher(teacherId))
            .totalStudents((int) dashboardRepository.countStudentsByTeacher(teacherId))
            .totalAssignments((int) dashboardRepository.countAssignmentsByTeacher(teacherId))
            .pendingGrading((int) dashboardRepository.countPendingGrading(teacherId))
            .gradeDistribution(gradeDist)
            .recentActivities(top5Activities)
            .upcomingDeadlinesTeacher(upcomingDeadlines)
            .build();
}


    public StudentDashboardResponse getStudentDashboard(Integer studentId) {
        List<UpcomingAssignmentDto> upcoming = assignmentService.getUpcomingAssignments(studentId);
        List<WeightedScorePerClassDTO> weightedList = gradeStatsService.getWeightedAveragePerClassByStudent(studentId);
        Double avgScore = null;
        if (!weightedList.isEmpty()) {
            avgScore = weightedList.stream()
                    .map(WeightedScorePerClassDTO::getTotalScore) // <-- total là điểm đã tính trọng số
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(Double.NaN);

            if (Double.isNaN(avgScore) == false) {
                avgScore = Math.round(avgScore * 100.0) / 100.0;
            } else {
                avgScore = null;
            }
        }


        return StudentDashboardResponse.builder()
                .enrolledClasses((int) dashboardRepository.countClassesByStudent(studentId))
                .totalAssignments(dashboardRepository.countAssignmentsByStudentId(studentId).intValue())
                .avgScore(avgScore)
                .completedAssignments(dashboardRepository.countAssignmentsTotal(studentId, null, null, null).intValue())
                .upcomingDeadlines(upcoming)
                .classProgress(dashboardRepository.findClassProgressByStudentId(studentId))
                .build();
    }
}

