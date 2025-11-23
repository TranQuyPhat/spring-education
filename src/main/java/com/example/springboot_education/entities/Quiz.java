package com.example.springboot_education.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "time_limit")
    private Integer timeLimit;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "class_id", nullable = true)
    private ClassEntity classField;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "subject")
    private String subject;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizQuestion> questions;

    @Column(name = "published_to_bank", nullable = false)
    private boolean publishedToBank = false;

    @Transient
    public QuizStatus getStatus() {
        Instant now = Instant.now();
        if (startDate != null && now.isBefore(startDate)) {
            return QuizStatus.UPCOMING;
        }
        if (endDate != null && now.isAfter(endDate)) {
            return QuizStatus.CLOSED;
        }
        return QuizStatus.OPEN;
    }

}