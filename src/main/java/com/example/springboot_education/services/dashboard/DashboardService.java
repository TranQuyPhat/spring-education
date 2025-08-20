package com.example.springboot_education.services.dashboard;


import com.example.springboot_education.dtos.classDTOs.ClassAggregateDTO;
import com.example.springboot_education.dtos.dashboard.TeacherDashboardSummaryDTO;

import java.util.List;

public interface DashboardService {
    TeacherDashboardSummaryDTO getTeacherSummary(Integer teacherId, Integer schoolYear, String semester);
    List<ClassAggregateDTO> getClassBreakdown(Integer teacherId, Integer schoolYear, String semester);

}