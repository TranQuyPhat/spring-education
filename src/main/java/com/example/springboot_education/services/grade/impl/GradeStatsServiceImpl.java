package com.example.springboot_education.services.grade.impl;


import com.example.springboot_education.dtos.gradeDTOs.GradeBase.BaseScoreStatsDTO;
import com.example.springboot_education.dtos.gradeDTOs.GradeBase.QuizAverageScoreDTO;
import com.example.springboot_education.dtos.gradeDTOs.GradeBase.WeightedScorePerClassDTO;
import com.example.springboot_education.repositories.assignment.SubmissionJpaRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import com.example.springboot_education.services.grade.GradeStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeStatsServiceImpl implements GradeStatsService {

    private final QuizSubmissionRepository quizRepo;
    private final SubmissionJpaRepository submissionRepo;

    @Override
    public List<QuizAverageScoreDTO> getAverageScorePerQuiz(Integer quizId) {
        return quizRepo.findAverageScorePerQuiz(quizId);
    }

    @Override
    public List<BaseScoreStatsDTO> getQuizAverageByClass(Integer classId) {
        return quizRepo.findAverageScoreQuizByClass(classId);
    }

    @Override
    public BaseScoreStatsDTO getAssignmentAverageForStudentInClass(Integer classId, Integer studentId) {
        return submissionRepo.findAssignmentAverageByClassAndStudent(classId, studentId);
    }

    @Override
    public List<BaseScoreStatsDTO> getAssignmentAverageAllClassesForStudent(Integer studentId) {
        return submissionRepo.findAssignmentAverageAllClassesByStudent(studentId);
    }


    @Override
    public List<BaseScoreStatsDTO> getOverallScorePerClassByStudent(Integer studentId) {
        List<BaseScoreStatsDTO> quizList = quizRepo.findQuizAvgPerClassByStudent(studentId);
        List<BaseScoreStatsDTO> assList = submissionRepo.findAssignmentAverageAllClassesByStudent(studentId);

        Map<Integer, BaseScoreStatsDTO> assMap = assList.stream()
                .collect(Collectors.toMap(BaseScoreStatsDTO::getClassId, a -> a));

        return quizList.stream().map(q -> {
            BaseScoreStatsDTO a = assMap.get(q.getClassId());

            double qScore = q.getAverageScore() != null ? q.getAverageScore() : 0.0;
            double aScore = a != null && a.getAverageScore() != null ? a.getAverageScore() : 0.0;
            double avg = (qScore + aScore) / 2.0;

            return new BaseScoreStatsDTO(
                    q.getClassId(),
                    q.getClassName(),
                    q.getStudentName(),
                    q.getStudentName(),
                    Math.round(avg * 100.0) / 100.0
            );
        }).collect(Collectors.toList());
    }

    @Override
    public List<BaseScoreStatsDTO> getStudentRankingByTeacher(Integer teacherId) {
        List<BaseScoreStatsDTO> quizList = quizRepo.findQuizAverageByTeacher(teacherId);
        List<BaseScoreStatsDTO> assignList = submissionRepo.findAssignmentAverageByTeacher(teacherId);

        // Gộp theo key classId + studentEmail
        Map<String, BaseScoreStatsDTO> resultMap = new HashMap<>();

        for (BaseScoreStatsDTO q : quizList) {
            String key = q.getClassId() + "_" + q.getStudentEmail();
            resultMap.put(key, new BaseScoreStatsDTO(
                    q.getClassId(), q.getClassName(), q.getStudentName(), q.getStudentEmail(),
                    q.getAverageScore() != null ? q.getAverageScore() : 0.0
            ));
        }

        for (BaseScoreStatsDTO a : assignList) {
            String key = a.getClassId() + "_" + a.getStudentEmail();
            BaseScoreStatsDTO entry = resultMap.getOrDefault(key, new BaseScoreStatsDTO(
                    a.getClassId(), a.getClassName(), a.getStudentName(), a.getStudentEmail(), 0.0
            ));

            double oldScore = entry.getAverageScore();
            double assignScore = a.getAverageScore() != null ? a.getAverageScore() : 0.0;

            entry.setAverageScore(Math.round((oldScore + assignScore) / 2.0 * 100.0) / 100.0);
            resultMap.put(key, entry);
        }

        return resultMap.values().stream()
                .sorted(Comparator.comparing(BaseScoreStatsDTO::getAverageScore).reversed())
                .collect(Collectors.toList());
    }


    @Override
    public List<BaseScoreStatsDTO> getOverallAverageByClass(Integer classId) {
        return List.of();
    }

    @Override
    public List<WeightedScorePerClassDTO> getWeightedAveragePerClassByStudent(Integer studentId) {
        List<WeightedScorePerClassDTO> quizList = quizRepo.findQuizAvgByClassAndStudent(studentId);
        List<WeightedScorePerClassDTO> assignList = submissionRepo.findAssignmentAvgByClassAndStudent(studentId);

        Map<Integer, WeightedScorePerClassDTO> quizMap = quizList.stream()
                .collect(Collectors.toMap(WeightedScorePerClassDTO::getClassId, q -> q));

        Map<Integer, WeightedScorePerClassDTO> assignMap = assignList.stream()
                .collect(Collectors.toMap(WeightedScorePerClassDTO::getClassId, a -> a));

        // Tập hợp tất cả classId
        Set<Integer> allClassIds = new HashSet<>();
        allClassIds.addAll(quizMap.keySet());
        allClassIds.addAll(assignMap.keySet());

        List<WeightedScorePerClassDTO> result = new ArrayList<>();

        for (Integer classId : allClassIds) {
            WeightedScorePerClassDTO quiz = quizMap.get(classId);
            WeightedScorePerClassDTO ass = assignMap.get(classId);

            Double quizAvg = quiz != null ? quiz.getQuizAvg() : null;
            Double assignAvg = ass != null ? ass.getAssignmentAvg() : null;

            double quizPart = quizAvg != null ? quizAvg * 0.5 : 10.0 * 0.5;
            double assignPart = assignAvg != null ? assignAvg * 0.4 : 10.0 * 0.4;
            double total = Math.round((quizPart + assignPart) * 100.0) / 100.0;

            WeightedScorePerClassDTO entry = new WeightedScorePerClassDTO(
                    classId,
                    quiz != null ? quiz.getClassName() : ass.getClassName(),
                    quiz != null ? quiz.getStudentName() : ass.getStudentName(),
                    quizAvg,
                    assignAvg,
                    total
            );
            result.add(entry);
        }

        return result;
    }

}