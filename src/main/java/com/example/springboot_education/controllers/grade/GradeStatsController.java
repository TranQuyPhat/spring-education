package com.example.springboot_education.controllers.grade;

import com.example.springboot_education.annotations.CurrentUser;
import com.example.springboot_education.dtos.gradeDTOs.GradeBase.BaseScoreStatsDTO;
import com.example.springboot_education.dtos.gradeDTOs.GradeBase.QuizAverageScoreDTO;
import com.example.springboot_education.dtos.gradeDTOs.GradeBase.WeightedScorePerClassDTO;
import com.example.springboot_education.dtos.quiz.APIResponse;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.services.grade.GradeStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class GradeStatsController {

    private final GradeStatsService gradeStatsService;

    @GetMapping("/student/{studentId}/weighted-average")
    public ResponseEntity<List<WeightedScorePerClassDTO>> getWeightedAverageScore(
            @PathVariable("studentId")  Integer studentId) {
        return ResponseEntity.ok(gradeStatsService.getWeightedAveragePerClassByStudent(studentId));
    }

    @GetMapping("/quiz/{quizId}/average")
    public ResponseEntity<List<QuizAverageScoreDTO>> getAverageScorePerQuiz(@PathVariable("quizId")  Integer quizId) {
        return ResponseEntity.ok(gradeStatsService.getAverageScorePerQuiz(quizId));
    }

    @GetMapping("/class/{classId}/quiz-average")
    public ResponseEntity<List<BaseScoreStatsDTO>> getQuizAverageByClass(@PathVariable("classId") Integer classId) {
        return ResponseEntity.ok(gradeStatsService.getQuizAverageByClass(classId));
    }

    @GetMapping("/class/{classId}/assignment-average/student/{studentId}")
    public ResponseEntity<BaseScoreStatsDTO> getAssignmentAverageByClassAndStudent(
            @PathVariable("classId") Integer classId,
            @PathVariable("studentId") Integer studentId) {
        return ResponseEntity.ok(gradeStatsService.getAssignmentAverageForStudentInClass(classId, studentId));
    }

    @GetMapping("/student/{studentId}/assignment-average")
    public ResponseEntity<List<BaseScoreStatsDTO>> getAssignmentAverageAllClassesForStudent(
            @PathVariable("studentId") Integer studentId) {
        return ResponseEntity.ok(gradeStatsService.getAssignmentAverageAllClassesForStudent(studentId));
    }

    @GetMapping("/class/{classId}/overall-average")
    public ResponseEntity<List<BaseScoreStatsDTO>> getOverallAverageByClass(@PathVariable("classId") Integer classId) {
        return ResponseEntity.ok(gradeStatsService.getOverallAverageByClass(classId));
    }

    @GetMapping("/student/{studentId}/overall-average")
    public ResponseEntity<List<BaseScoreStatsDTO>> getOverallAveragePerClass(
            @PathVariable("studentId")  Integer studentId) {
        return ResponseEntity.ok(gradeStatsService.getOverallScorePerClassByStudent(studentId));
    }

    @GetMapping("/teacher/ranking")
    public ResponseEntity<APIResponse<List<BaseScoreStatsDTO>>> getRanking(@CurrentUser Users currentUser) {
        Integer teacherId = currentUser.getId();
        List<BaseScoreStatsDTO> ranking = gradeStatsService.getStudentRankingByTeacher(teacherId);

        APIResponse<List<BaseScoreStatsDTO>> response =
                new APIResponse<>(true, "Lấy bảng xếp hạng thành công", ranking);

        return ResponseEntity.ok(response);
    }

}