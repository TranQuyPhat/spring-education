package com.example.springboot_education.services;

import java.sql.Timestamp;
import java.util.*;
import org.springframework.stereotype.Service;

import com.example.springboot_education.dtos.assignmentDTOs.UpcomingAssignmentDto;
import com.example.springboot_education.dtos.assignmentDTOs.UpcomingSubmissionDto;
import com.example.springboot_education.dtos.dashboardsClient.DashboardActivityDTO;
import com.example.springboot_education.dtos.dashboardsClient.StudentDashboardResponse;
import com.example.springboot_education.dtos.dashboardsClient.TeacherDashboardResponse;
import com.example.springboot_education.repositories.DashboardRepository;
import com.example.springboot_education.services.assignment.AssignmentService;
import com.example.springboot_education.untils.TimeAgoUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final AssignmentService assignmentService;

    public TeacherDashboardResponse getTeacherDashboard(Integer teacherId) {

        List<DashboardActivityDTO> recentActivities = new ArrayList<>();
        List<UpcomingSubmissionDto> upcomingDeadlines = assignmentService.getUpcomingSubmissions(teacherId);

        // --- 1. Lấy recent submissions ---
        List<Object[]> submissions = dashboardRepository.countSubmissionsPerAssignmentByTeacher(teacherId);
        // Object[]: [assignmentId, assignmentTitle, className, countStudents,
        // latestSubmittedAt]
        for (Object[] row : submissions) {
            DashboardActivityDTO dto = new DashboardActivityDTO();
            dto.setMessage(row[3] + " học sinh đã nộp bài tập " + row[1]);
            dto.setType("submission");
            dto.setClassName((String) row[2]);
            Timestamp ts = (Timestamp) row[4];
            if (ts != null) {
                dto.setTime(TimeAgoUtil.timeAgo(ts.toInstant()));
                dto.setSortTime(ts.toInstant()); // lưu Instant thực tế để sort
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
                dto.setSortTime(ts.toInstant()); // lưu Instant thực tế để sort
            }
            recentActivities.add(dto);
        }
        // --- 3. Sort theo thời gian giảm dần và lấy 5 gần nhất ---
        recentActivities.sort((a, b) -> b.getSortTime().compareTo(a.getSortTime()));
        List<DashboardActivityDTO> top5Activities = recentActivities.stream()
                .limit(5)
                .toList();

        // --- 4. Build dashboard response ---
        return TeacherDashboardResponse.builder()
                .totalClasses((int) dashboardRepository.countClassesByTeacher(teacherId))
                .totalStudents((int) dashboardRepository.countStudentsByTeacher(teacherId))
                .totalAssignments((int) dashboardRepository.countAssignmentsByTeacher(teacherId))
                .pendingGrading((int) dashboardRepository.countPendingGrading(teacherId))
                .averageGrade(
                        Optional.ofNullable(dashboardRepository.findAverageScoreByTeacher(teacherId))
                                .orElse(0.0))
                .recentActivities(top5Activities)
                .upcomingDeadlinesTeacher(upcomingDeadlines)
                .build();
    }

    public StudentDashboardResponse getStudentDashboard(Integer studentId) {
        List<UpcomingAssignmentDto> upcoming = assignmentService.getUpcomingAssignments(studentId);

        return StudentDashboardResponse.builder()
                .enrolledClasses((int) dashboardRepository.countClassesByStudent(studentId))
                .totalAssignments(dashboardRepository.countAssignmentsByStudentId(studentId).intValue())
                .completedAssignments(dashboardRepository.countAssignmentsTotal(studentId, null, null, null).intValue())
                .upcomingDeadlines(upcoming)
                .classProgress(dashboardRepository.findClassProgressByStudentId(studentId))
                .build();
    }

}
