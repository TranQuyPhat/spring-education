package com.example.springboot_education.entities;

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
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "assignments")
public class Assignment {
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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classField;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @NotNull
    @Column(name = "max_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxScore;

    @Size(max = 500)
    @Column(name = "file_path", length = 500)
    private String filePath;

    @Size(max = 255)
    @Column(name = "file_type", length = 255)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize; // bytes

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "is_published", nullable = false)
    private boolean published = false;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        updatedAt = createdAt;
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }

      @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private Set<Submission> submissions;

}