package com.example.springboot_education.services.grade;

import com.example.springboot_education.dtos.gradeDTOs.*;
import com.example.springboot_education.repositories.ClassUserRepository;
import com.example.springboot_education.repositories.SubjectRepository;
import com.example.springboot_education.repositories.assignment.SubmissionJpaRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentGradesService {

    private final SubmissionJpaRepository submissionJpaRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final SubjectRepository subjectRepository;
    private final ClassUserRepository classUserRepository;

    private static final BigDecimal ASSIGN_WEIGHT = BigDecimal.valueOf(0.4);
    private static final BigDecimal QUIZ_WEIGHT = BigDecimal.valueOf(0.6);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    // Helper: safe convert Object(Double/BigDecimal/Number) -> BigDecimal
    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Double) return BigDecimal.valueOf((Double) o);
        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
        try {
            return new BigDecimal(o.toString());
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    // Weighted avg for a student (optionally filtered)
    public BigDecimal calcWeightedAvg(Integer studentId, Integer classId, Integer subjectId,
                                      LocalDateTime startDate, LocalDateTime endDate) {
        Double avgAssignD = submissionJpaRepository.avgAssignmentScore(studentId, subjectId, classId, startDate, endDate);
        Double avgQuizD = quizSubmissionRepository.avgQuizScore(studentId, subjectId, classId, startDate, endDate);
        BigDecimal avgAssign = BigDecimal.valueOf(avgAssignD != null ? avgAssignD : 0.0);
        BigDecimal avgQuiz = BigDecimal.valueOf(avgQuizD != null ? avgQuizD : 0.0);
        BigDecimal weighted = avgAssign.multiply(ASSIGN_WEIGHT).add(avgQuiz.multiply(QUIZ_WEIGHT));
        return weighted.setScale(2, RoundingMode.HALF_UP);
    }

    // 1. Overview
    public StudentOverviewDTO getOverview(Integer studentId, Integer subjectId, LocalDateTime startDate, LocalDateTime endDate) {
//        // Convert cho Submission (Timestamp / LocalDateTime)
//        LocalDateTime startDateTimeLocal = startDate != null ? startDate.atStartOfDay() : null;
//        LocalDateTime endDateTimeLocal = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
//
//        // Convert cho QuizSubmission (Instant)
//        Instant startDateTimeInstant = startDate != null ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
//        Instant endDateTimeInstant = endDate != null ? endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant() : null;

        // Gọi repository cho Assignment
        long totalAssign = Optional.ofNullable(submissionJpaRepository.countAssignmentsTotal(studentId, subjectId, startDate, endDate)).orElse(0L);
        long gradedAssign = Optional.ofNullable(submissionJpaRepository.countAssignmentsGraded(studentId, subjectId, startDate, endDate)).orElse(0L);
        long pendingAssign = totalAssign - gradedAssign;

        // Gọi repository cho Quiz
        long totalQuiz = Optional.ofNullable(quizSubmissionRepository.countQuizzesTotal(studentId, subjectId, startDate, endDate)).orElse(0L);
        long gradedQuiz = Optional.ofNullable(quizSubmissionRepository.countQuizzesGraded(studentId, subjectId, startDate, endDate)).orElse(0L);

        // Lấy danh sách điểm
        List<Double> assignScores = submissionJpaRepository.findAssignmentScores(studentId, subjectId, startDate, endDate);
        List<Double> quizScores = quizSubmissionRepository.findQuizScores(studentId, subjectId, startDate, endDate);

        List<Double> allScores = new ArrayList<>();
        if (assignScores != null) allScores.addAll(assignScores);
        if (quizScores != null) allScores.addAll(quizScores);

        // Tính weighted avg
        BigDecimal weightedAvg = calcWeightedAvg(studentId, null, subjectId, startDate, endDate);

        List<ScoreRangeDTO> distribution = computeDistribution(allScores);

        return new StudentOverviewDTO(
                weightedAvg,
                new CountDto(totalAssign, gradedAssign, Math.max(0, pendingAssign)),
                new CountDto(totalQuiz, gradedQuiz, Math.max(0, totalQuiz - gradedQuiz)),
                distribution
        );
    }



    private List<ScoreRangeDTO> computeDistribution(List<Double> scores) {
        long c0 = scores.stream().filter(d -> d != null && d < 4.0).count();
        long c1 = scores.stream().filter(d -> d != null && d >= 4.0 && d < 6.0).count();
        long c2 = scores.stream().filter(d -> d != null && d >= 6.0 && d < 8.0).count();
        long c3 = scores.stream().filter(d -> d != null && d >= 8.0).count();
        return List.of(
                new ScoreRangeDTO("0-4", c0),
                new ScoreRangeDTO("4-6", c1),
                new ScoreRangeDTO("6-8", c2),
                new ScoreRangeDTO("8-10", c3)
        );
    }

    // 2. By-subject averages (list subjects the student has scores in OR all subjects)
    public List<SubjectAnalysisDTO> getBySubject(Integer studentId) {
        List<Object[]> allSubjects = subjectRepository.findAllSubjectsIdName();
        Map<Integer, String> subMap = allSubjects.stream()
                .collect(Collectors.toMap(o -> (Integer) o[0], o -> (String) o[1]));

        List<SubjectAnalysisDTO> out = new ArrayList<>();
        for (Map.Entry<Integer, String> e : subMap.entrySet()) {
            Integer subjectId = e.getKey();
            BigDecimal avg = calcWeightedAvg(studentId, null, subjectId, null, null);
            out.add(new SubjectAnalysisDTO(subjectId, e.getValue(), avg));
        }
        return out;
    }

    // 3. Student details
    public StudentDetailsDTO getDetails(Integer studentId, Integer subjectId) {
        List<Object[]> assignRows = submissionJpaRepository.findAssignmentDetails(studentId, subjectId);
        List<AssignmentDetailDTO> assignments = assignRows.stream().map(r -> {
            Integer id = (Integer) r[0];
            String title = (String) r[1];
            Object scObj = r[2];
            String status = r[3] != null ? r[3].toString() : null;
            Object dueObj = r[4];
            BigDecimal score = toBigDecimal(scObj);
            String due = null;
            if (dueObj != null) {
                // dueObj is Instant in Assignment.dueDate
                if (dueObj instanceof java.time.Instant) {
                    due = ((Instant) dueObj).toString();
                } else due = dueObj.toString();
            }
            return new AssignmentDetailDTO(id, title, score, status, due);
        }).collect(Collectors.toList());

        List<Object[]> quizRows = quizSubmissionRepository.findQuizDetails(studentId, subjectId);
        List<QuizDetailDTO> quizzes = quizRows.stream().map(r -> {
            Integer qid = (Integer) r[0];
            String title = (String) r[1];
            Object sc = r[2];
            Object submittedAt = r[3];
            BigDecimal score = toBigDecimal(sc);
            String subAt = submittedAt != null ? submittedAt.toString() : null;
            return new QuizDetailDTO(qid, title, score, subAt);
        }).collect(Collectors.toList());

        return new StudentDetailsDTO(assignments, quizzes);
    }

    // 4. Trends (daily avg combined weighted)
    public List<TrendDTO> getTrends(Integer studentId, Integer subjectId, String timeUnit) {
        // get assignment averages per date
        List<Object[]> assignByDate = submissionJpaRepository.findAvgAssignmentByDate(studentId, subjectId);
        List<Object[]> quizByDate = quizSubmissionRepository.avgQuizScoreByDate(studentId, subjectId);

        // merge by date string
        Map<String, BigDecimal> map = new TreeMap<>();
        for (Object[] r : assignByDate) {
            Object dateObj = r[0];
            Object avgObj = r[1];
            String date = dateObj != null ? dateObj.toString().substring(0, 10) : null;
            BigDecimal avg = toBigDecimal(avgObj).multiply(ASSIGN_WEIGHT);
            if (date != null) map.put(date, avg);
        }
        for (Object[] r : quizByDate) {
            Object dateObj = r[0];
            Object avgObj = r[1];
            String date = dateObj != null ? dateObj.toString().substring(0, 10) : null;
            BigDecimal avg = toBigDecimal(avgObj).multiply(QUIZ_WEIGHT);
            map.merge(date, avg, BigDecimal::add);
        }
        return map.entrySet().stream()
                .map(e -> new TrendDTO(e.getKey(), e.getValue().setScale(2, RoundingMode.HALF_UP)))
                .collect(Collectors.toList());
    }

    // 5. Compare with class average
    public CompareDto compareWithClass(Integer studentId, Integer classId, Integer subjectId) {
        BigDecimal studentAvg = calcWeightedAvg(studentId, classId, subjectId, null, null);
        Double classAvgD = classUserRepository.classWeightedAvg(classId, subjectId); // may be null
        BigDecimal classAvg = BigDecimal.valueOf(classAvgD != null ? classAvgD : 0.0).setScale(2, RoundingMode.HALF_UP);
        BigDecimal diff = studentAvg.subtract(classAvg).setScale(2, RoundingMode.HALF_UP);
        return new CompareDto(studentAvg, classAvg, diff);
    }
}
