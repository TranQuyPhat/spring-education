package com.example.springboot_education.services.dashboard.student;

import com.example.springboot_education.dtos.dashboard.student.AssignmentDTO;
import com.example.springboot_education.dtos.dashboard.student.RecentScoreDTO;
import com.example.springboot_education.dtos.dashboard.student.SubjectGradeDTO;
import com.example.springboot_education.repositories.dashboard.student.StudentScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentScoreService {

    @Autowired
    private StudentScoreRepository studentScoreRepository;

    public List<RecentScoreDTO> getRecentScores(Integer studentId) {
        List<Object[]> results = studentScoreRepository.findRecentScoresByStudentId(studentId);
        List<RecentScoreDTO> recentScores = new ArrayList<>();

        for (Object[] result : results) {
            String className = (String) result[0];
            String subjectName = (String) result[1];
            Timestamp submittedAt = (Timestamp) result[2];
            BigDecimal score = (BigDecimal) result[3];
            String type = (String) result[4];
            String title = (String) result[5];

            recentScores.add(new RecentScoreDTO(
                    className,
                    subjectName,
                    submittedAt.toLocalDateTime(),
                    score,
                    type,
                    title
            ));
        }

        return recentScores;
    }

    public List<SubjectGradeDTO> getAllClassResults(Integer studentId) {
        List<Object[]> results = studentScoreRepository.findAllResultsByStudentId(studentId);
    System.out.println("Student id:" +studentId);
        // Group by class_id and subject_name
        Map<String, List<Object[]>> groupedResults = results.stream()
                .collect(Collectors.groupingBy(result ->
                        result[0] + "_" + result[2] // class_id + subject_name
                ));

        List<SubjectGradeDTO> subjectGrades = new ArrayList<>();
        int id = 1;

        for (Map.Entry<String, List<Object[]>> entry : groupedResults.entrySet()) {
            List<Object[]> subjectResults = entry.getValue();
            Object[] firstResult = subjectResults.get(0);

            Long classIdLong = (Long) firstResult[0];
            Integer classId = classIdLong.intValue();
            String className = (String) firstResult[1];
            String subjectName = (String) firstResult[2];

            // Convert to AssignmentDTO list
            List<AssignmentDTO> assignments = subjectResults.stream()
                    .map(result -> {
                        String title = (String) result[3];
                        String type = (String) result[4];
                        Timestamp submittedAt = (Timestamp) result[5];
                        BigDecimal score = (BigDecimal) result[6];

                        return new AssignmentDTO(
                                title,
                                score,
                                BigDecimal.valueOf(10), // Default max grade = 10
                                type,
                                submittedAt.toLocalDateTime()
                        );
                    })
                    .collect(Collectors.toList());

            // Calculate average
            BigDecimal average = calculateAverage(assignments);

            // Calculate trend (simplified logic - you can make it more sophisticated)
            String trend = calculateTrend(assignments);

            subjectGrades.add(new SubjectGradeDTO(
                    id++,
                    subjectName,
                    className,
                    assignments,
                    average,
                    trend
            ));
        }

        return subjectGrades;
    }

    private BigDecimal calculateAverage(List<AssignmentDTO> assignments) {
        if (assignments.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalScore = assignments.stream()
                .map(AssignmentDTO::getGrade)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalScore.divide(BigDecimal.valueOf(assignments.size()), 2, RoundingMode.HALF_UP);
    }

    private String calculateTrend(List<AssignmentDTO> assignments) {
        if (assignments.size() < 2) {
            return "stable";
        }

        // Sort by date to get chronological order
        assignments.sort(Comparator.comparing(AssignmentDTO::getDate));

        // Compare last two assignments
        AssignmentDTO recent = assignments.get(assignments.size() - 1);
        AssignmentDTO previous = assignments.get(assignments.size() - 2);

        int comparison = recent.getGrade().compareTo(previous.getGrade());

        if (comparison > 0) {
            return "up";
        } else if (comparison < 0) {
            return "down";
        } else {
            return "stable";
        }
    }
}