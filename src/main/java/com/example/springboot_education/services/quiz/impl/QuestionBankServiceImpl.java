package com.example.springboot_education.services.quiz.impl;

import com.example.springboot_education.entities.*;
import com.example.springboot_education.repositories.quiz.QuestionBankOptionRepository;
import com.example.springboot_education.repositories.quiz.QuestionBankRepository;
import com.example.springboot_education.repositories.quiz.QuizOptionRepository;
import com.example.springboot_education.repositories.quiz.QuizQuestionRepository;
import com.example.springboot_education.repositories.quiz.QuizRepository;
import com.example.springboot_education.services.quiz.QuestionBankService;
import com.example.springboot_education.untils.QuizUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionBankServiceImpl implements QuestionBankService {
    private final QuestionBankRepository questionBankRepository;
    private final QuestionBankOptionRepository questionBankOptionRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;

    @Override
    @Transactional
    public int publishQuizQuestionsToBank(Integer quizId, List<Integer> questionIds, Integer userId, boolean isAdmin) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found: " + quizId));
        if ((questionIds == null || questionIds.isEmpty()) && quiz.isPublishedToBank()) {
            return 0;
        }

        List<QuizQuestion> questions = quizQuestionRepository.findQuestionsWithOptionsByQuizId(quizId);
        if (questionIds != null && !questionIds.isEmpty()) {
            questions.removeIf(q -> !questionIds.contains(q.getId()));
        }

        int created = 0;
        for (QuizQuestion qq : questions) {
            if (qq.getBankQuestionId() != null)
                continue;

            nQuestion nq = new nQuestion();
            nq.setQuestionText(qq.getQuestionText());
            nq.setQuestionType(qq.getQuestionType());
            if (qq.getQuiz() != null) {

                if (qq.getQuiz().getClassField() != null) {
                    nq.setSubject(qq.getQuiz().getClassField().getSubject());
                }
                nq.setCreatedBy(qq.getQuiz().getCreatedBy());
            }
            nq.setCorrectOptions(qq.getCorrectOptions());
            nq.setCorrectTrueFalse(qq.getCorrectTrueFalse());
            nq.setCorrectAnswerRegex(qq.getCorrectAnswerRegex());
            nq.setCaseSensitive(qq.isCaseSensitive());
            nq.setTrimWhitespace(qq.isTrimWhitespace());
            nq.setApprovalStatus(nQuestion.ApprovalStatus.APPROVED);
            nq.setVisibility(nQuestion.Visibility.PUBLIC);
            Users approver = new Users();
            approver.setId(userId);
            nq.setApprovedBy(approver);
            nq.setApprovedAt(java.time.LocalDateTime.now());

            nq = questionBankRepository.save(nq);

            // map back to quiz question to avoid future duplicates
            qq.setBankQuestionId(nq.getId());
            quizQuestionRepository.save(qq);

            // copy options
            List<QuizOption> opts = qq.getOptions();
            if (opts != null) {
                for (QuizOption o : opts) {
                    nQuestionOption no = new nQuestionOption();
                    no.setQuestion(nq);
                    no.setOptionLabel(o.getOptionLabel());
                    no.setOptionText(o.getOptionText());
                    questionBankOptionRepository.save(no);
                }
            }
            created++;
        }

        // If publishing the whole quiz (not a selective publish), mark quiz as
        // published
        if (questionIds == null || questionIds.isEmpty()) {
            quiz.setPublishedToBank(true);
            quizRepository.save(quiz);
        }

        return created;
    }

}
