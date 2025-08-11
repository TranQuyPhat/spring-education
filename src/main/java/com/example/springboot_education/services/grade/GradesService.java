package com.example.springboot_education.services.grade;

import com.example.springboot_education.dtos.gradeDTOs.*;
import com.example.springboot_education.entities.QuizSubmission;
import com.example.springboot_education.entities.Submission;
import com.example.springboot_education.repositories.assignment.SubmissionJpaRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradesService {

    private final SubmissionJpaRepository submissionRepo;
    private final QuizSubmissionRepository quizSubmissionRepo;

    public GradesOverviewDTO getOverview(Integer classId, Integer subjectId, Instant startDate, Instant endDate) {
        BigDecimal avgScore = calcWeightedAvgForAllStudents(classId, subjectId, startDate, endDate);

        long gradedCount = submissionRepo.countGraded();
        long lateCount = submissionRepo.countLate();
        long missingCount = submissionRepo.countMissing();

        List<ScoreRangeDTO> distribution = List.of(
                new ScoreRangeDTO("0-4", 5),
                new ScoreRangeDTO("4-6", 20),
                new ScoreRangeDTO("6-8", 60),
                new ScoreRangeDTO("8-10", 35)
        );

        return new GradesOverviewDTO(avgScore, gradedCount, lateCount, missingCount, distribution);
    }

    private BigDecimal calcWeightedAvgForAllStudents(Integer classId, Integer subjectId, Instant startDate, Instant endDate) {
        // TODO: Implement real logic to get all students and average
        // For now, mock a value:
        return BigDecimal.valueOf(7.8).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcWeightedAvg(Integer studentId, Integer classId, Integer subjectId, Instant startDate, Instant endDate) {
        BigDecimal avgAssign = submissionRepo.avgAssignmentScore1(studentId, subjectId, classId, startDate, endDate);
        BigDecimal avgQuiz = quizSubmissionRepo.avgQuizScore1(studentId, subjectId, classId, startDate, endDate);

        return avgAssign.multiply(BigDecimal.valueOf(0.4))
                .add(avgQuiz.multiply(BigDecimal.valueOf(0.6)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public List<StudentRankingDTO> getRankings(Integer classId, Integer subjectId, Integer limit) {
        List<Object[]> studentsAssign = submissionRepo.findAllStudentsForRanking();
        List<StudentRankingDTO> rankings = studentsAssign.stream()
                .map(obj -> {
                    Integer studentId = (Integer) obj[0];
                    String fullName = (String) obj[1];
                    BigDecimal avg = calcWeightedAvg(studentId, classId, subjectId, null, null);
                    return new StudentRankingDTO(studentId, fullName, avg, 0);
                })
                .sorted((a, b) -> b.getAvgScore().compareTo(a.getAvgScore()))
                .limit(limit)
                .collect(Collectors.toList());

        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }
        return rankings;
    }

    public StudentRankPositionDTO getStudentRank(Integer studentId, Integer classId, Integer subjectId) {
        List<StudentRankingDTO> all = getRankings(classId, subjectId, Integer.MAX_VALUE);
        int position = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getStudentId().equals(studentId)) {
                position = i + 1;
                break;
            }
        }
        BigDecimal avg = calcWeightedAvg(studentId, classId, subjectId, null, null);
        return new StudentRankPositionDTO(studentId, position, all.size(), avg);
    }

    public StudentDetailsDTO getStudentDetails(Integer studentId) {
        List<AssignmentDetailDTO> assignments = submissionRepo.findAssignmentsByStudent(studentId)
                .stream()
                .map(r -> new AssignmentDetailDTO(
                        (Integer) r[0],
                        (String) r[1],
                        (BigDecimal) r[2],
                        (String) r[3],
                        (String) r[4]
                ))
                .collect(Collectors.toList());

        List<QuizDetailDTO> quizzes = quizSubmissionRepo.findQuizzesByStudent(studentId)
                .stream()
                .map(r -> new QuizDetailDTO(
                        (Integer) r[0],
                        (String) r[1],
                        (BigDecimal) r[2],
                        (String) r[3]
                ))
                .collect(Collectors.toList());

        return new StudentDetailsDTO(assignments, quizzes);
    }

    public List<SubjectAnalysisDTO> getSubjectAnalysis() {
        List<Object[]> assign = submissionRepo.avgAssignmentBySubject();
        List<Object[]> quiz = quizSubmissionRepo.avgQuizBySubject();

        Map<Integer, SubjectAnalysisDTO> map = new HashMap<>();

        for (Object[] a : assign) {
            Integer subjectId = (Integer) a[0];
            String name = (String) a[1];
            Double scoreVal = (Double) a[2];
            BigDecimal score = BigDecimal.valueOf(scoreVal != null ? scoreVal : 0.0);
            map.put(subjectId, new SubjectAnalysisDTO(subjectId, name, score.multiply(BigDecimal.valueOf(0.4))));
        }

        for (Object[] q : quiz) {
            Integer subjectId = (Integer) q[0];
            String name = (String) q[1];
            Double scoreVal = (Double) q[2];
            BigDecimal score = BigDecimal.valueOf(scoreVal != null ? scoreVal : 0.0);
            map.merge(subjectId,
                    new SubjectAnalysisDTO(subjectId, name, score.multiply(BigDecimal.valueOf(0.6))),
                    (oldVal, newVal) -> {
                        oldVal.setAvgScore(oldVal.getAvgScore().add(newVal.getAvgScore()));
                        return oldVal;
                    }
            );
        }
        return new ArrayList<>(map.values());
    }

    public List<TrendDTO> getTrends(Integer classId, Integer subjectId, String timeUnit) {
        List<Object[]> assign = submissionRepo.avgAssignmentByDate();
        List<Object[]> quiz = quizSubmissionRepo.avgQuizByDate();

        Map<String, TrendDTO> map = new LinkedHashMap<>();

        for (Object[] a : assign) {
            String date = a[0].toString();
            Double scoreVal = (Double) a[1];
            BigDecimal score = BigDecimal.valueOf(scoreVal != null ? scoreVal : 0.0);
            map.put(date, new TrendDTO(date, score.multiply(BigDecimal.valueOf(0.4))));
        }

        for (Object[] q : quiz) {
            String date = q[0].toString();
            Double scoreVal = (Double) q[1];
            BigDecimal score = BigDecimal.valueOf(scoreVal != null ? scoreVal : 0.0);
            map.merge(date,
                    new TrendDTO(date, score.multiply(BigDecimal.valueOf(0.6))),
                    (oldVal, newVal) -> {
                        oldVal.setAvgScore(oldVal.getAvgScore().add(newVal.getAvgScore()));
                        return oldVal;
                    }
            );
        }
        return new ArrayList<>(map.values());
    }

    //    student
    public List<GradeResultDTO> getStudentResults(Integer classId, Integer studentId) {
        List<GradeResultDTO> results = new ArrayList<>();

        // Lấy điểm bài tập
        List<Submission> submissions = submissionRepo.findByStudentIdAndAssignment_ClassField_Id(studentId, classId);
        submissions.forEach(s -> results.add(
                GradeResultDTO.builder()
                        .type("ASSIGNMENT")
                        .title(s.getAssignment().getTitle())
                        .score(s.getScore())
                        .maxScore(s.getAssignment().getMaxScore())
                        .submittedAt(s.getSubmittedAt().toInstant())
                        .gradedAt(s.getGradedAt() != null ? s.getGradedAt().toInstant() : null)
                        .status(s.getStatus() != null ? s.getStatus().name() : null)
                        .build()
        ));

        // Lấy điểm quiz
        List<QuizSubmission> quizSubs = quizSubmissionRepo.findByStudentIdAndQuiz_ClassField_Id(studentId, classId);
        quizSubs.forEach(q -> results.add(
                GradeResultDTO.builder()
                        .type("QUIZ")
                        .title(q.getQuiz().getTitle())
                        .score(q.getScore())
                        .maxScore(null) // Quiz không có maxScore trong entity, có thể bổ sung nếu cần
                        .submittedAt(q.getSubmittedAt())
                        .gradedAt(q.getGradedAt())
                        .status(q.getScore() != null ? "GRADED" : "SUBMITTED")
                        .build()
        ));

        return results;
    }
}
