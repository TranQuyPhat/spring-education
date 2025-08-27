package com.example.springboot_education.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType questionType;
    @NotNull
    @Lob
    @Column(name = "question_text", nullable = false)
    private String questionText;


    @ColumnDefault("1.00")
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;


    @Column(name = "correct_options")
    private String correctOptions;
    @Column(name = "correct_true_false")
    private Boolean correctTrueFalse;
    @ElementCollection
    @CollectionTable(
            name = "question_correct_texts",
            joinColumns = @JoinColumn(name = "question_id")
    )
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private Set<String> correctAnswerTexts = new LinkedHashSet<>();
    @Column(name = "answer_regex", length = 1024)
    private String correctAnswerRegex;
    @Column(name = "case_sensitive")
    private boolean caseSensitive = false;

    @Column(name = "trim_whitespace")
    private boolean trimWhitespace = true;

    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<QuizOption> options =new ArrayList<>();


    /* =========================================
       Helpers cho correctOptions (A,B,...)
       - MULTI_CHOICE strict: chọn thiếu/thừa là SAI
       - ONE_CHOICE: đúng 1 nhãn
       ========================================= */

    @Transient
    public Set<String> getCorrectOptionSet() {
        if (correctOptions == null || correctOptions.isBlank()) return Collections.emptySet();
        return Arrays.stream(correctOptions.split(","))
                .map(s -> s == null ? "" : s.trim().toUpperCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Lưu set nhãn về chuỗi normalized "A,B" */
    public void setCorrectOptionSet(Collection<String> labels) {
        if (labels == null || labels.isEmpty()) {
            this.correctOptions = null;
        } else {
            String normalized = labels.stream()
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toUpperCase())
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.joining(","));
            this.correctOptions = normalized.isEmpty() ? null : normalized;
        }
    }
  /* =========================================
       Logic chấm đúng/sai theo yêu cầu:
       - MULTI_CHOICE: strict equality (không partial credit)
       - ONE_CHOICE: 1 chọn == 1 đáp án
       - TRUE_FALSE: so boolean
       - FILL_BLANK: so text list và/hoặc regex
       ========================================= */

    /**
     * @param userOptionLabels các nhãn user chọn (A,B,...) cho chọn đáp án.
     * @param userText câu trả lời text cho FILL_BLANK (có thể null).
     */
    public boolean isUserAnswerCorrect(List<String> userOptionLabels, String userText) {
        switch (questionType) {
            case ONE_CHOICE: {
                // cần đúng 1 nhãn và trùng với đúng 1 đáp án
                Set<String> correct = getCorrectOptionSet();
                if (correct.size() != 1) return false;
                if (userOptionLabels == null || userOptionLabels.size() != 1) return false;
                String u = norm(userOptionLabels.get(0));
                return correct.contains(u);
            }
            case MULTI_CHOICE: {
                // strict: phải khớp tập hợp 100% (thiếu/thừa là SAI)
                Set<String> correct = getCorrectOptionSet();
                Set<String> user = normalizeToSet(userOptionLabels);
                return !correct.isEmpty() && user.equals(correct);
            }
            case TRUE_FALSE: {
                if (correctTrueFalse == null) return false;
                if (userOptionLabels == null || userOptionLabels.size() != 1) return false;
                String v = norm(userOptionLabels.get(0));
                Boolean userVal = "TRUE".equals(v) ? Boolean.TRUE :
                        "FALSE".equals(v) ? Boolean.FALSE : null;
                return userVal != null && userVal.equals(correctTrueFalse);
            }
            case FILL_BLANK: {
                if (userText == null) return false;
                String u = trimWhitespace ? userText.trim() : userText;
                if (!caseSensitive) u = u.toLowerCase();

                // match text list
                if (correctAnswerTexts != null && !correctAnswerTexts.isEmpty()) {
                    for (String ans : correctAnswerTexts) {
                        if (ans == null) continue;
                        String a = trimWhitespace ? ans.trim() : ans;
                        if (!caseSensitive) a = a.toLowerCase();
                        if (u.equals(a)) return true;
                    }
                }
                // match regex
                if (correctAnswerRegex != null && !correctAnswerRegex.isBlank()) {
                    try {
                        if (u.matches(correctAnswerRegex)) return true;
                    } catch (Exception ignored) { /* regex lỗi => coi như không dùng */ }
                }
                return false;
            }
            default:
                return false;
        }
    }

    private String norm(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    private Set<String> normalizeToSet(Collection<String> in) {
        if (in == null || in.isEmpty()) return Collections.emptySet();
        return in.stream()
                .filter(Objects::nonNull)
                .map(this::norm)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        // normalize correctOptions lúc lưu
        if (this.correctOptions != null) {
            setCorrectOptionSet(getCorrectOptionSet());
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        if (this.correctOptions != null) {
            setCorrectOptionSet(getCorrectOptionSet());
        }
    }

}
