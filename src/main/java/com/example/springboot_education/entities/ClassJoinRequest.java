package com.example.springboot_education.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "class_join_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Quan hệ với bảng ClassEntity
    @ManyToOne(fetch = FetchType.LAZY) // hoặc EAGER nếu muốn load ngay
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    // Quan hệ với bảng User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Users student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
