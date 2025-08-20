package com.example.springboot_education.services.grade;


import com.example.springboot_education.dtos.gradeDTOs.GradeBase.BaseScoreStatsDTO;
import com.example.springboot_education.dtos.gradeDTOs.GradeBase.QuizAverageScoreDTO;
import com.example.springboot_education.dtos.gradeDTOs.GradeBase.WeightedScorePerClassDTO;

import java.util.List;

public interface GradeStatsService {
    List<QuizAverageScoreDTO> getAverageScorePerQuiz(Integer quizId);
    List<BaseScoreStatsDTO> getQuizAverageByClass(Integer classId);
    BaseScoreStatsDTO getAssignmentAverageForStudentInClass(Integer classId, Integer studentId);
    List<BaseScoreStatsDTO> getAssignmentAverageAllClassesForStudent(Integer studentId);
    List<BaseScoreStatsDTO> getOverallAverageByClass(Integer classId);
    List<WeightedScorePerClassDTO> getWeightedAveragePerClassByStudent(Integer studentId);

    List<BaseScoreStatsDTO> getOverallScorePerClassByStudent(Integer studentId);
    List<BaseScoreStatsDTO> getStudentRankingByTeacher(Integer teacherId);


}