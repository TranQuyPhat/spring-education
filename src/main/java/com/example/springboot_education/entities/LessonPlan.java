package com.example.springboot_education.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;

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
@Table(name = "lesson_plans")
public class LessonPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classRoom;

    @Column(nullable = false)
    private Integer sessionNumber; // buổi số mấy (1,2,3,...)

    @Column(length = 255, nullable = false)
    private String title; // VD: "Chương 1: Giới thiệu"

    @Column(length = 500)
    private String description; // chi tiết hơn nếu cần
}
