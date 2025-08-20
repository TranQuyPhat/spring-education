package com.example.springboot_education.services.dashboard.impl;


import com.example.springboot_education.dtos.classDTOs.ClassAggregateDTO;
import com.example.springboot_education.dtos.dashboard.TeacherDashboardSummaryDTO;
import com.example.springboot_education.repositories.classes.ClassAggregateRepository;
import com.example.springboot_education.services.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClassAggregateRepository classAggregateRepo;

    @Override
    public List<ClassAggregateDTO> getClassBreakdown(Integer teacherId, Integer schoolYear, String semester) {
        return classAggregateRepo.aggregatePerClass(teacherId, schoolYear, semester);
    }
    @Override
    public TeacherDashboardSummaryDTO getTeacherSummary(Integer teacherId, Integer schoolYear, String semester) {
        List<ClassAggregateDTO> list = getClassBreakdown(teacherId, schoolYear, semester);

        long totalClasses = list.size();
        long totalStudents = list.stream().mapToLong(ClassAggregateDTO::getStudents).sum();
        long totalAssignments = list.stream().mapToLong(ClassAggregateDTO::getAssignments).sum();
        long totalDocuments = list.stream().mapToLong(ClassAggregateDTO::getMaterials).sum();
        long totalSubmissions = list.stream().mapToLong(ClassAggregateDTO::getSubmissions).sum();

        return new TeacherDashboardSummaryDTO(
                teacherId,
                totalClasses,
                totalStudents,
                totalAssignments,
                totalDocuments,
                totalSubmissions
        );
    }
}
