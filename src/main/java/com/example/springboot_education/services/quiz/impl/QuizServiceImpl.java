package com.example.springboot_education.services.quiz.impl;

import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.assignmentDTOs.NotificationAssignmentDTO;
import com.example.springboot_education.dtos.quiz.OptionDTO;
import com.example.springboot_education.dtos.quiz.QuestionsPageResponseDTO;
import com.example.springboot_education.dtos.quiz.QuizContentUpdateDTO;
import com.example.springboot_education.dtos.quiz.QuizRequestDTO;
import com.example.springboot_education.dtos.quiz.base.QuestionBaseDTO;
import com.example.springboot_education.dtos.quiz.base.QuizBaseDTO;
import com.example.springboot_education.dtos.quiz.student.QuestionStudentDTO;
import com.example.springboot_education.dtos.quiz.student.QuizResponseStudentDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuestionTeacherDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuizResponseTeacherDTO;
import com.example.springboot_education.entities.QuestionType;
import com.example.springboot_education.entities.Quiz;
import com.example.springboot_education.entities.QuizOption;
import com.example.springboot_education.entities.QuizQuestion;
import com.example.springboot_education.mapper.QuizMapper2;
import com.example.springboot_education.repositories.classes.ClassUserRepository;
import com.example.springboot_education.repositories.quiz.QuizOptionRepository;
import com.example.springboot_education.repositories.quiz.QuizQuestionRepository;
import com.example.springboot_education.repositories.quiz.QuizRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import com.example.springboot_education.services.SlackService;
import com.example.springboot_education.services.assignment.NotificationServiceAssignment;
import com.example.springboot_education.services.quiz.QuizAccessService;
import com.example.springboot_education.services.quiz.QuizService;
import com.example.springboot_education.untils.QuizUtils;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final com.example.springboot_education.repositories.quiz.QuestionBankRepository questionBankRepository;
    private final com.example.springboot_education.repositories.quiz.QuestionBankOptionRepository questionBankOptionRepository;
    private final QuizMapper2 quizMapper2;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final ClassUserRepository classUserRepository;
    private final QuizAccessService quizAccessService;
    private final SlackService slackService;
    private final NotificationServiceAssignment notificationService;

    @Override
    @Transactional
    @LoggableAction(value = "CREATE", entity = "quizzes", description = "Created new quiz")
    public QuizBaseDTO createQuiz(QuizRequestDTO quizReqDTO) {
        Quiz quiz = quizMapper2.toEntity(quizReqDTO);
        quiz = quizRepository.save(quiz);
        for (QuestionTeacherDTO qdto : quizReqDTO.getQuestions()) {
            QuizQuestion question = new QuizQuestion();
            question.setQuiz(quiz);
            // support creating question from question bank
            if (qdto.getSourceQuestionId() != null) {
                Long srcId = qdto.getSourceQuestionId();
                com.example.springboot_education.entities.nQuestion bank = questionBankRepository.findById(srcId)
                        .orElseThrow(() -> new RuntimeException("Question bank entry not found: " + srcId));
                question.setQuestionText(bank.getQuestionText());
                question.setQuestionType(
                        bank.getQuestionType() == null ? QuestionType.ONE_CHOICE : bank.getQuestionType());
                question.setCreatedAt(Instant.now());
                question.setUpdatedAt(Instant.now());
                question.setCorrectOptions(bank.getCorrectOptions());
                question.setCorrectTrueFalse(bank.getCorrectTrueFalse());
                question.setCorrectAnswerRegex(bank.getCorrectAnswerRegex());
                question.setCaseSensitive(bank.isCaseSensitive());
                question.setTrimWhitespace(bank.isTrimWhitespace());

                question = questionRepository.save(question);

                // copy options from bank
                List<com.example.springboot_education.entities.nQuestionOption> opts = questionBankOptionRepository
                        .findByQuestion_Id(srcId);
                Set<String> seen = new HashSet<>();
                for (com.example.springboot_education.entities.nQuestionOption opt : opts) {
                    String label = opt.getOptionLabel() == null ? "" : opt.getOptionLabel().trim().toUpperCase();
                    if (label.isEmpty())
                        continue;
                    if (!seen.add(label))
                        continue;
                    QuizOption option = new QuizOption();
                    option.setQuestion(question);
                    option.setOptionLabel(label);
                    option.setOptionText(opt.getOptionText());
                    option.setCreatedAt(Instant.now());
                    option.setUpdatedAt(Instant.now());
                    optionRepository.save(option);
                }
            } else {
                question.setQuestionText(qdto.getQuestionText());
                question.setQuestionType(qdto.getQuestionType());
                question.setCreatedAt(Instant.now());
                question.setUpdatedAt(Instant.now());
                String normalized = QuizUtils.normalizeCorrectOptions(qdto.getCorrectOptions());
                QuizUtils.validateByType(qdto.getQuestionType(), normalized);
                question.setCorrectOptions(normalized);
                if (qdto.getQuestionType() == QuestionType.TRUE_FALSE) {
                    question.setCorrectTrueFalse("TRUE".equalsIgnoreCase(normalized));
                }

                if (qdto.getQuestionType() == QuestionType.FILL_BLANK) {
                    question.setCorrectAnswerTexts(new HashSet<>(qdto.getCorrectAnswerTexts()));
                    question.setCorrectAnswerRegex(qdto.getCorrectAnswerRegex());
                    question.setCaseSensitive(qdto.isCaseSensitive());
                    question.setTrimWhitespace(qdto.isTrimWhitespace());
                }

                question = questionRepository.save(question);

                Set<String> seen = new HashSet<>();
                for (OptionDTO opt : qdto.getOptions()) {
                    String label = (opt.getOptionLabel() == null ? "" : opt.getOptionLabel().trim().toUpperCase());
                    if (label.isEmpty()) {
                        throw new ValidationException("optionLabel is required");
                    }
                    if (!seen.add(label)) {
                        throw new ValidationException("Duplicate option label: " + label);
                    }

                    QuizOption option = new QuizOption();
                    option.setQuestion(question);
                    option.setOptionLabel(label);
                    option.setOptionText(opt.getOptionText());
                    option.setCreatedAt(Instant.now());
                    option.setUpdatedAt(Instant.now());
                    optionRepository.save(option);
                }
            }
            // continue to next question
        }
        Map<String, Object> payload = Map.of(
                "quizTitle", quiz.getTitle());
        slackService.sendSlackNotification(
                quiz.getClassField().getId(),
                SlackService.ClassEventType.QUIZ_CREATED,
                payload);
        NotificationAssignmentDTO notifyPayload = NotificationAssignmentDTO.builder()
                .classId(quiz.getClassField().getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .dueDate(quiz.getEndDate())
                .message("Có Quiz mới được giao, vui lòng kiểm tra!")
                .build();
        System.out.println("---- Quiz Created Notification Payload ----");
        System.out.println(notifyPayload);
        System.out.println("classIdquiz: " + quiz.getClassField().getId());

        notificationService.notifyClass(quiz.getClassField().getId(), notifyPayload);
        return getQuizForTeacher(quiz.getId());
    }

    // @Cacheable(value = "quizzesByTeacher", key = "#teacherId")
    @Override
    public List<QuizResponseTeacherDTO> getQuizzesByTeacherId(Integer teacherId) {
        List<Quiz> quizzes = quizRepository.findByCreatedBy_Id(teacherId);

        return quizzes.stream().map(quiz -> {
            QuizResponseTeacherDTO dto = new QuizResponseTeacherDTO();
            quizMapper2.mapBaseFields(quiz, dto);
            dto.setClassId(quiz.getClassField().getId());
            dto.setCreatedBy(teacherId);
            dto.setClassName(quiz.getClassField().getClassName());
            int totalStudents = classUserRepository.countByClassField_Id(quiz.getClassField().getId());
            int submitted = quizSubmissionRepository.countByQuiz_Id(quiz.getId());
            dto.setTotalStudents(totalStudents);
            dto.setStudentsSubmitted(submitted);
            dto.setStudentsUnSubmitted(Math.max(0, totalStudents - submitted));
            int totalQuestion = questionRepository.countByQuiz_Id(quiz.getId());
            dto.setTotalQuestion(totalQuestion);

            return dto;
        }).toList();
    }

    @Override
    public com.example.springboot_education.dtos.quiz.TeacherOverviewDTO getTeacherOverview(Integer teacherId,
            int latestPerClass) {
        var rows = quizRepository.findLatestQuizzesPerClass(teacherId, latestPerClass);
        // map classId -> ClassOverviewDTO
        java.util.Map<Integer, com.example.springboot_education.dtos.quiz.ClassOverviewDTO> map = new java.util.LinkedHashMap<>();

        for (var r : rows) {
            Integer classId = r.getClass_id();
            String className = r.getClass_name();
            com.example.springboot_education.dtos.quiz.ClassOverviewDTO c = map.computeIfAbsent(classId, k -> {
                com.example.springboot_education.dtos.quiz.ClassOverviewDTO dto = new com.example.springboot_education.dtos.quiz.ClassOverviewDTO();
                dto.setClassId(k);
                dto.setClassName(className);
                dto.setLatestQuizzes(new java.util.ArrayList<>());
                dto.setTotalQuizzes(0);
                return dto;
            });
            com.example.springboot_education.dtos.quiz.QuizSummaryDTO qs = new com.example.springboot_education.dtos.quiz.QuizSummaryDTO();
            qs.setId(r.getQuiz_id());
            qs.setTitle(r.getTitle());
            qs.setDescription(r.getDescription());
            qs.setStartDate(r.getStart_date());
            qs.setEndDate(r.getEnd_date());
            qs.setSubject(r.getSubject());
            qs.setTotalQuestions(r.getTotal_questions());
            c.getLatestQuizzes().add(qs);
        }

        List<Object[]> counts = quizRepository.countQuizzesByTeacherGrouped(teacherId);
        for (Object[] row : counts) {
            Integer classId = ((Number) row[0]).intValue();
            Integer cnt = ((Number) row[1]).intValue();
            var c = map.get(classId);
            if (c != null)
                c.setTotalQuizzes(cnt);
            else {
                com.example.springboot_education.dtos.quiz.ClassOverviewDTO dto = new com.example.springboot_education.dtos.quiz.ClassOverviewDTO();
                dto.setClassId(classId);
                dto.setClassName(null);
                dto.setLatestQuizzes(new java.util.ArrayList<>());
                dto.setTotalQuizzes(cnt);
                map.put(classId, dto);
            }
        }

        com.example.springboot_education.dtos.quiz.TeacherOverviewDTO out = new com.example.springboot_education.dtos.quiz.TeacherOverviewDTO();
        out.setTeacherId(teacherId);
        out.setClasses(new java.util.ArrayList<>(map.values()));
        return out;
    }

    @Override
    public org.springframework.data.domain.Page<com.example.springboot_education.dtos.quiz.QuizSummaryDTO> getQuizzesByClass(
            Integer classId, int page, int size) {
        if (page < 1)
            page = 1;
        if (size < 1)
            size = 10;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page - 1,
                size, org.springframework.data.domain.Sort.by("id").descending());
        var p = quizRepository.findByClassField_Id(classId, pageable);
        var items = p.getContent().stream().map(q -> {
            com.example.springboot_education.dtos.quiz.QuizSummaryDTO s = new com.example.springboot_education.dtos.quiz.QuizSummaryDTO();
            s.setId(q.getId());
            s.setTitle(q.getTitle());
            s.setDescription(q.getDescription());
            s.setStartDate(q.getStartDate());
            s.setEndDate(q.getEndDate());
            s.setSubject(q.getSubject());
            long count = questionRepository.countByQuiz_Id(q.getId());
            s.setTotalQuestions((int) count);
            return s;
        }).toList();
        return new org.springframework.data.domain.PageImpl<>(items, pageable, p.getTotalElements());
    }

    @Override
    public List<QuizResponseTeacherDTO> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(q -> getQuizForTeacher(q.getId()))
                .toList();
    }

    @Override
    public List<QuizResponseStudentDTO> getQuizzesByStudentId(Integer studentId) {
        List<Object[]> rows = quizRepository.findBasicQuizzesByStudentId(studentId);
        List<QuizResponseStudentDTO> quizzes = new ArrayList<>();

        for (Object[] row : rows) {
            QuizResponseStudentDTO dto = new QuizResponseStudentDTO();

            dto.setId((Integer) row[0]);
            dto.setTitle((String) row[1]);
            dto.setDescription((String) row[2]);
            dto.setTimeLimit(row[3] != null ? ((Number) row[3]).intValue() : null);
            dto.setStartDate(QuizUtils.convertToOffsetDateTime(row[4]));
            dto.setEndDate(QuizUtils.convertToOffsetDateTime(row[5]));
            dto.setSubject((String) row[6]);
            dto.setClassName((String) row[7]);
            dto.setTotalQuestion(row[8] != null ? ((Number) row[8]).intValue() : 0);

            if (row[9] != null) {
                dto.setScore(((Number) row[9]).doubleValue());
            }

            boolean submitted = row[11] != null && ((Number) row[11]).intValue() == 1;
            dto.setSubmitted(submitted);

            quizzes.add(dto);
        }
        return quizzes;
    }

    @Override
    public QuizResponseTeacherDTO getQuizForTeacher(Integer quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        List<QuizQuestion> questions = questionRepository.findQuestionsWithOptionsByQuizId(quizId);
        List<QuestionTeacherDTO> questionDTOs = questions.stream()
                .map(question -> {
                    List<OptionDTO> optionDTOs = question.getOptions()
                            .stream().map(quizMapper2::toOptionDto).toList();
                    return quizMapper2.toTeacherQuestionDto(question, optionDTOs);
                })
                .toList();

        return quizMapper2.toTeacherDto(quiz, questionDTOs);
    }

    @Override
    public QuizResponseStudentDTO getQuizForStudent(Integer quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        List<QuizQuestion> questions = questionRepository.findQuestionsWithOptionsByQuizId(quizId);

        List<QuestionStudentDTO> questionDTOs = questions.stream()
                .map(question -> {
                    List<QuizOption> options = optionRepository.findByQuestion_Id(question.getId());
                    List<OptionDTO> optionDTOs = options.stream()
                            .map(quizMapper2::toOptionDto)
                            .toList();
                    return quizMapper2.toStudentQuestionDto(question, optionDTOs);
                })
                .toList();

        return quizMapper2.toStudentDto(quiz, questionDTOs);
    }

    public QuizResponseStudentDTO getQuizForStudent(Integer quizId, Integer studentId) {
        Quiz quiz = quizAccessService.assertStudentCanAccess(quizId, studentId);
        var questions = questionRepository.findQuestionsWithOptionsByQuizId(quizId);
        var qDtos = questions.stream().map(q -> {
            var opts = optionRepository.findByQuestion_Id(q.getId());
            var oDtos = opts.stream().map(quizMapper2::toOptionDto).toList();
            return quizMapper2.toStudentQuestionDto(q, oDtos);
        }).toList();
        return quizMapper2.toStudentDto(quiz, qDtos);
    }

    @Transactional(readOnly = true)
    public QuestionsPageResponseDTO<QuestionStudentDTO> getQuizQuestionsPageForStudent(
            Integer quizId, int page, int size, Integer studentId) {
        quizAccessService.assertStudentCanAccess(quizId, studentId);
        return getQuizQuestionsPage(
                quizId, page, size,
                (q, optsByQid) -> {
                    var opts = optsByQid.getOrDefault(q.getId(), List.of());
                    return quizMapper2.toStudentQuestionDto(q, opts);
                });
    }

    @Override
    @Transactional
    @LoggableAction(value = "UPDATE", entity = "quizzes", description = "Update quiz")
    public QuizResponseTeacherDTO updateQuizMeta(Integer quizId, QuizBaseDTO dto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        if (dto.getTitle() != null)
            quiz.setTitle(dto.getTitle());
        if (dto.getDescription() != null)
            quiz.setDescription(dto.getDescription());
        if (dto.getTimeLimit() != null)
            quiz.setTimeLimit(dto.getTimeLimit());
        if (dto.getStartDate() != null)
            quiz.setStartDate(dto.getStartDate().toInstant());
        if (dto.getEndDate() != null)
            quiz.setEndDate(dto.getEndDate().toInstant());
        quiz.setUpdatedAt(Instant.now());
        quizRepository.save(quiz);
        return getQuizForTeacher(quizId);
    }

    @Override
    @Transactional
    public QuizResponseTeacherDTO updateQuizContent(Integer quizId, QuizContentUpdateDTO body) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        List<QuizQuestion> existedQuestions = questionRepository.findQuestionsWithOptionsByQuizId(quizId);
        Map<Integer, QuizQuestion> qMap = existedQuestions.stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        Set<Integer> seenQuestionIds = new HashSet<>();

        for (QuestionTeacherDTO qdto : body.getQuestions()) {
            QuizQuestion q;
            if (qdto.getId() == null) {
                q = new QuizQuestion();
                q.setQuiz(quiz);
                q.setCreatedAt(Instant.now());
            } else {
                q = qMap.get(qdto.getId());
                if (q == null)
                    throw new RuntimeException("Question not found: " + qdto.getId());
                seenQuestionIds.add(q.getId());
            }
            q.setQuestionText(qdto.getQuestionText());
            q.setCorrectOptions(qdto.getCorrectOptions());
            q.setUpdatedAt(Instant.now());
            q = questionRepository.save(q);

            List<QuizOption> existedOpts = optionRepository.findByQuestion_Id(q.getId());
            Map<Integer, QuizOption> oMap = existedOpts.stream()
                    .collect(Collectors.toMap(QuizOption::getId, o -> o));
            Set<Integer> seenOptIds = new HashSet<>();

            for (OptionDTO odto : qdto.getOptions()) {
                QuizOption opt;
                if (odto.getId() == null) {
                    opt = new QuizOption();
                    opt.setQuestion(q);
                    opt.setCreatedAt(Instant.now());
                } else {
                    opt = oMap.get(odto.getId());
                    if (opt == null)
                        throw new RuntimeException("Option not found: " + odto.getId());
                    seenOptIds.add(opt.getId());
                }
                opt.setOptionLabel(odto.getOptionLabel());
                opt.setOptionText(odto.getOptionText());
                opt.setUpdatedAt(Instant.now());
                optionRepository.save(opt);
            }

            // Xóa option thừa nếu replaceAll
            if (body.isReplaceAll()) {
                existedOpts.stream()
                        .filter(o -> !seenOptIds.contains(o.getId()))
                        .forEach(optionRepository::delete);
            }
        }

        // Xóa question thừa nếu replaceAll
        if (body.isReplaceAll()) {
            existedQuestions.stream()
                    .filter(q -> !seenQuestionIds.contains(q.getId())
                            && body.getQuestions().stream().noneMatch(dto -> Objects.equals(dto.getId(), q.getId())))
                    .forEach(questionRepository::delete);
        }

        quiz.setUpdatedAt(Instant.now());
        quizRepository.save(quiz);

        return getQuizForTeacher(quizId);
    }

    @Override
    @Transactional
    public void deleteQuestion(Integer quizId, Integer questionId) {
        QuizQuestion q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        if (!q.getQuiz().getId().equals(quizId)) {
            throw new RuntimeException("Question does not belong to quiz");
        }
        // orphanRemoval + OnDelete CASCADE ở options sẽ lo phần con
        questionRepository.delete(q);
    }

    @Override
    @LoggableAction(value = "DELETE", entity = "quizzes", description = "Delete quiz")
    @Transactional
    public void deleteQuiz(Integer quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        quizSubmissionRepository.deleteAll(quizSubmissionRepository.findByQuiz_Id(quizId));
        quizRepository.delete(quiz);
    }

    private <T extends QuestionBaseDTO> QuestionsPageResponseDTO<T> getQuizQuestionsPage(
            Integer quizId,
            int page,
            int size,
            BiFunction<QuizQuestion, Map<Integer, List<OptionDTO>>, T> mapper) {
        if (page < 1)
            page = 1;
        if (size < 1)
            size = 10;

        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<QuizQuestion> questionPage = questionRepository.findByQuiz_Id(quizId, pageable);

        List<Integer> qIds = questionPage.getContent().stream()
                .map(QuizQuestion::getId)
                .toList();

        List<QuizOption> options = optionRepository.findByQuestion_IdIn(qIds);

        Map<Integer, List<OptionDTO>> optionsByQid = options.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getQuestion().getId(),
                        Collectors.mapping(quizMapper2::toOptionDto, Collectors.toList())));

        List<T> items = questionPage.getContent().stream()
                .map(q -> mapper.apply(q, optionsByQid))
                .toList();

        return new QuestionsPageResponseDTO<>(quizId, page, size, questionPage.getTotalElements(), items);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionsPageResponseDTO<QuestionTeacherDTO> getQuizQuestionsPageForTeacher(Integer quizId, int page,
            int size) {
        return getQuizQuestionsPage(
                quizId,
                page,
                size,
                (q, optsByQid) -> {
                    List<OptionDTO> opts = optsByQid.getOrDefault(q.getId(), List.of());
                    return quizMapper2.toTeacherQuestionDto(q, opts);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionsPageResponseDTO<QuestionStudentDTO> getQuizQuestionsPageForStudent(Integer quizId, int page,
            int size) {
        return getQuizQuestionsPage(
                quizId,
                page,
                size,
                (q, optsByQid) -> {
                    List<OptionDTO> opts = optsByQid.getOrDefault(q.getId(), List.of());
                    return quizMapper2.toStudentQuestionDto(q, opts);
                });
    }

}