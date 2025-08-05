package com.example.springboot_education.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "submissions")
public class Submission {
    public enum SubmissionStatus {
        SUBMITTED,
        GRADED,
        LATE,
        MISSING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Users student;

    @NotNull
    @Column(name = "submitted_at", nullable = false)
    private Timestamp submittedAt;

    @Size(max = 255)
    @NotNull
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Size(max = 255)
    @NotNull
    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubmissionStatus status;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "graded_at")
    private Timestamp gradedAt;

    @Lob
    @Column(name = "teacher_comment")
    private String teacherComment;

    @PrePersist
    protected void onSubmit() {
        submittedAt = new Timestamp(System.currentTimeMillis());
    }

    public boolean isGraded() {
        return score != null;
    }

    public boolean isLate() {
        if (assignment == null || assignment.getDueDate() == null || submittedAt == null) return false;
        return submittedAt.after(assignment.getDueDate());
    }

}