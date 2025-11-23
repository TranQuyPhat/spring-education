package com.example.springboot_education.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class nQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Users approvedBy;

    private LocalDateTime approvedAt;

    private String correctOptions;
    private Boolean correctTrueFalse;
    private String correctAnswerRegex;

    @Column(columnDefinition = "boolean default false")
    private boolean caseSensitive;

    @Column(columnDefinition = "boolean default true")
    private boolean trimWhitespace;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<nQuestionOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<nQuestionCorrectText> correctTexts = new ArrayList<>();

    public enum Visibility {
        PRIVATE, PUBLIC
    }

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }
}