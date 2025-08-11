package com.example.springboot_education.services.quiz.impl;

import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO;
import com.example.springboot_education.dtos.quiz.OptionDTO;
import com.example.springboot_education.dtos.quiz.QuestionDTO;
import com.example.springboot_education.dtos.quiz.QuizContentUpdateDTO;
import com.example.springboot_education.dtos.quiz.QuizRequestDTO;
import com.example.springboot_education.dtos.quiz.base.QuizBaseDTO;
import com.example.springboot_education.dtos.quiz.student.QuestionStudentDTO;
import com.example.springboot_education.dtos.quiz.student.QuizResponseStudentDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuestionTeacherDTO;
import com.example.springboot_education.dtos.quiz.teacher.QuizResponseTeacherDTO;
import com.example.springboot_education.entities.Quiz;
import com.example.springboot_education.entities.QuizOption;
import com.example.springboot_education.entities.QuizQuestion;
import com.example.springboot_education.mapper.QuizMapper2;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.quiz.QuizOptionRepository;
import com.example.springboot_education.repositories.quiz.QuizQuestionRepository;
import com.example.springboot_education.repositories.quiz.QuizRepository;
import com.example.springboot_education.repositories.quiz.QuizSubmissionRepository;
import com.example.springboot_education.services.ActivityLogService;
import com.example.springboot_education.services.quiz.QuizService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final QuizMapper2 quizMapper2;
    private final QuizSubmissionRepository quizSubmissionRepository;

    private final ActivityLogService activityLogService;
    @Override
    public QuizBaseDTO createQuiz(QuizRequestDTO quizDTO) {
        Quiz quiz = quizMapper2.toEntity(quizDTO);
        quiz = quizRepository.save(quiz);

        for (QuestionDTO qdto : quizDTO.getQuestions()) {
            QuizQuestion question = new QuizQuestion();
            question.setQuiz(quiz);
            question.setQuestionText(qdto.getQuestionText());
            question.setCorrectOption(qdto.getCorrectOption().charAt(0));
            question.setScore(qdto.getScore());
            question.setCreatedAt(Instant.now());
            question.setUpdatedAt(Instant.now());
            question = questionRepository.save(question);

            for (OptionDTO opt : qdto.getOptions()) {
                QuizOption option = new QuizOption();
                option.setQuestion(question);
                option.setOptionLabel(opt.getOptionLabel());
                option.setOptionText(opt.getOptionText());
                option.setCreatedAt(Instant.now());
                option.setUpdatedAt(Instant.now());
                optionRepository.save(option);
            }
        }

        return getQuizForTeacher(quiz.getId());
    }


    @Override
    public List<QuizResponseTeacherDTO> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(q -> getQuizForTeacher(q.getId()))
                .toList();
    }

    @Override
    public QuizResponseTeacherDTO getQuizForTeacher(Integer quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        List<QuizQuestion> questions = questionRepository.findByQuiz_Id(quizId);

        List<QuestionTeacherDTO> questionDTOs = questions.stream()
                .map(question -> {
                    List<QuizOption> options = optionRepository.findByQuestion_Id(question.getId());
                    List<OptionDTO> optionDTOs = options.stream()
                            .map(quizMapper2::toOptionDto)
                            .toList();
                    return quizMapper2.toTeacherQuestionDto(question, optionDTOs);
                })
                .toList();

        return quizMapper2.toTeacherDto(quiz, questionDTOs);
    }

    @Override
    public QuizResponseStudentDTO getQuizForStudent(Integer quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        List<QuizQuestion> questions = questionRepository.findByQuiz_Id(quizId);

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

    @Override
    @Transactional
    public QuizResponseTeacherDTO updateQuizMeta(Integer quizId, QuizBaseDTO dto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        if (dto.getTitle() != null) quiz.setTitle(dto.getTitle());
        if (dto.getDescription() != null) quiz.setDescription(dto.getDescription());
        if (dto.getTimeLimit() != null) quiz.setTimeLimit(dto.getTimeLimit());
        if (dto.getStartDate() != null) quiz.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) quiz.setEndDate(dto.getEndDate());
        if (dto.getGrade() != null) quiz.setGrade(dto.getGrade());
        if (dto.getSubject() != null) quiz.setSubject(dto.getSubject());

        quiz.setUpdatedAt(Instant.now());
        quizRepository.save(quiz);
        return getQuizForTeacher(quizId);
    }
    @Override
    @Transactional
    public QuizResponseTeacherDTO updateQuizContent(Integer quizId, QuizContentUpdateDTO body) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Map các question/option hiện có để tiện tra cứu
        List<QuizQuestion> existedQuestions = questionRepository.findByQuiz_Id(quizId);
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
                if (q == null) throw new RuntimeException("Question not found: " + qdto.getId());
                seenQuestionIds.add(q.getId());
            }
            q.setQuestionText(qdto.getQuestionText());
            q.setCorrectOption(qdto.getCorrectOption());
            q.setScore(qdto.getScore());
            q.setUpdatedAt(Instant.now());
            q = questionRepository.save(q);

            // --- xử lý options ---
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
                    if (opt == null) throw new RuntimeException("Option not found: " + odto.getId());
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
    @Transactional
    public void deleteQuiz(Integer quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        quizSubmissionRepository.deleteAll(quizSubmissionRepository.findByQuiz_Id(quizId));

        quizRepository.delete(quiz);
        activityLogService.log(new ActivityLogCreateDTO(
                "DELETE",
                quiz.getId(),
                "quizzes",
                "Xóa quiz: " + quiz.getTitle(),
                quiz.getClassField() != null ? quiz.getClassField().getId() : null,null
        ));
    }


}
