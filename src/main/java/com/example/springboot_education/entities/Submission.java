package com.example.springboot_education.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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
    private LocalDateTime submittedAt;

    @Size(max = 500)
    @NotNull
    @Column(name = "file_path", length = 500)
    private String filePath;

    @Size(max = 255)
    @NotNull
    @Column(name = "file_type", length = 255)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize; // bytes

    @Lob
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubmissionStatus status;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Lob
    @Column(name = "teacher_comment")
    private String teacherComment;

    @PrePersist
    protected void onSubmit() {
        submittedAt = LocalDateTime.now();
    }

    public boolean isGraded() {
        return score != null && status == SubmissionStatus.GRADED;
    }

    public boolean isLate() {
        if (assignment == null || assignment.getDueDate() == null || submittedAt == null) return false;
        return submittedAt.isAfter(assignment.getDueDate());
    }

}