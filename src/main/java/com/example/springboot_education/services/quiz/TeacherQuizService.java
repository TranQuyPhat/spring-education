package com.example.springboot_education.services.quiz;

import com.example.springboot_education.dtos.quiz.ClassQuizDTO;
import com.example.springboot_education.dtos.quiz.GroupedQuizDTO;
import com.example.springboot_education.dtos.quiz.QuizDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.Quiz;
import com.example.springboot_education.entities.QuizStatus;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.classes.ClassUserRepository;
import com.example.springboot_education.repositories.quiz.QuizRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherQuizService {
    private final ClassRepository classRepo;
    private final QuizRepository quizRepo;
    private final ClassUserRepository classUserRepo;
    private final QuizSubmissionRepository quizSubmissionRepo;
    public Page<QuizDTO> getQuizDtosByClassAndStatus(
            Integer classId, QuizStatus status, int page, int size) {

        Page<Quiz> quizPage = getQuizzesByClassAndStatus(classId, status, page, size);

        return quizPage.map(q -> {
            QuizDTO dto = new QuizDTO();
            // --- map các field kế thừa từ QuizBaseDTO ---
            dto.setId(q.getId());
            dto.setTitle(q.getTitle());
            dto.setDescription(q.getDescription());
            dto.setTimeLimit(q.getTimeLimit());
            dto.setStartDate(q.getStartDate() != null ? q.getStartDate().atOffset(ZoneOffset.UTC) : null);
            dto.setEndDate(q.getEndDate() != null ? q.getEndDate().atOffset(ZoneOffset.UTC) : null);
            dto.setSubject(q.getSubject());
            dto.setClassName(q.getClassField().getClassName());
            dto.setTotalQuestion(q.getQuestions() != null ? q.getQuestions().size() : 0);

            // --- field riêng của QuizDTO ---
            dto.setStatus(q.getStatus());
            int totalStudents = classUserRepo.countByClassField_Id(q.getClassField().getId());
            int submitted = quizSubmissionRepo.countByQuiz_Id(q.getId());
            dto.setTotalStudents(totalStudents);
            dto.setStudentsSubmitted(submitted);
            return dto;
        });
    }public GroupedQuizDTO getGroupedQuizzesByTeacher(
            Integer teacherId,
            QuizStatus status,
            int classPage, int classSize,
            int quizPageSize) {

        Page<ClassEntity> classPageRes =
                classRepo.findByTeacher_Id(teacherId, PageRequest.of(classPage, classSize));

        // Lọc ra chỉ những lớp có quiz theo status
        List<ClassQuizDTO> classDtos = classPageRes.getContent().stream()
                .filter(cls -> {
                    // Kiểm tra xem lớp có quiz theo status không
                    long quizCount = countQuizzesByClassAndStatus(cls.getId(), status);
                    return quizCount > 0;
                })
                .map(cls -> {
                    // đếm tổng số quiz của lớp theo trạng thái
                    long quizTotal = countQuizzesByClassAndStatus(cls.getId(), status);

                    // lấy quiz page đầu tiên
                    Page<Quiz> quizPage = getQuizzesByClassAndStatus(cls.getId(), status, 0, quizPageSize);

                    List<QuizDTO> quizDtos = quizPage.getContent().stream().map(q -> {
                        QuizDTO dto = new QuizDTO();
                        dto.setId(q.getId());
                        dto.setTitle(q.getTitle());
                        dto.setDescription(q.getDescription());
                        dto.setTimeLimit(q.getTimeLimit());
                        dto.setStartDate(q.getStartDate() != null ? q.getStartDate().atOffset(ZoneOffset.UTC) : null);
                        dto.setEndDate(q.getEndDate() != null ? q.getEndDate().atOffset(ZoneOffset.UTC) : null);
                        dto.setSubject(q.getSubject());
                        dto.setClassName(cls.getClassName());
                        dto.setTotalQuestion(q.getQuestions() != null ? q.getQuestions().size() : 0);
                        dto.setStatus(q.getStatus());
                        int totalStudents = classUserRepo.countByClassField_Id(cls.getId());
                        int submitted = quizSubmissionRepo.countByQuiz_Id(q.getId());
                        dto.setTotalStudents(totalStudents);
                        dto.setStudentsSubmitted(submitted);
                        return dto;
                    }).toList();

                    ClassQuizDTO cDto = new ClassQuizDTO();
                    cDto.setClassId(cls.getId());
                    cDto.setClassName(cls.getClassName());
                    cDto.setSubjectName(cls.getSubject().getSubjectName());
                    cDto.setQuizTotal(quizTotal);
                    cDto.setQuizzes(quizDtos);
                    return cDto;
                }).toList();

        return new GroupedQuizDTO(
                classDtos,
                classPageRes.getNumber(),
                classPageRes.getTotalPages()
        );
    }

    private Page<Quiz> getQuizzesByClassAndStatus(
            Integer classId, QuizStatus status, int page, int size) {
        Instant now = Instant.now();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return switch (status) {
            case UPCOMING -> quizRepo.findByClassField_IdAndStartDateAfter(classId, now, pageable);
            case CLOSED   -> quizRepo.findByClassField_IdAndEndDateBefore(classId, now, pageable);
            default       -> quizRepo.findByClassField_IdAndStartDateBeforeAndEndDateAfter(
                    classId, now, now, pageable);
        };
    }

    private long countQuizzesByClassAndStatus(Integer classId, QuizStatus status) {
        Instant now = Instant.now();
        return switch (status) {
            case UPCOMING -> quizRepo.countByClassField_IdAndStartDateAfter(classId, now);
            case CLOSED   -> quizRepo.countByClassField_IdAndEndDateBefore(classId, now);
            default       -> quizRepo.countByClassField_IdAndStartDateBeforeAndEndDateAfter(
                    classId, now, now);
        };
    }
}
