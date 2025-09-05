package com.example.springboot_education.entities;


import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
@Entity
@Table(name = "class_schedule_session")
public class ClassScheduleSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id", nullable = false)
    private ClassSchedulePattern pattern;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "start_period", nullable = false)
    private Integer startPeriod;

    @Column(name = "end_period", nullable = false)
    private Integer endPeriod;

    @Column(name = "location")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(name = "note")
    private String note;


    @Column(name = "submitted_at")
    private Instant submittedAt;

    public enum SessionStatus {
        SCHEDULED,   // Buổi học theo lịch
        CANCELLED,   // Buổi học bị hủy
        HOLIDAY,     // Nghỉ lễ
        MAKEUP,      // Học bù
        COMPLETED    // Đã diễn ra
    }
}

    


