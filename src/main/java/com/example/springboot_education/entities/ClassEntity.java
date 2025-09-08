package com.example.springboot_education.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "classes")
public class ClassEntity {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "class_name", nullable = false, length = 100)
    private String className;

    @NotNull
    @Column(name = "school_year", nullable = false)
    private Integer schoolYear;

    @NotNull
    @Lob
    @Column(name = "semester", nullable = false)
    private String semester;

    @Lob
    @Column(name = "description")
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Users teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @OneToMany(mappedBy = "classField", fetch = FetchType.LAZY)
    private List<ClassUser> classUsers;

    // @OneToMany(mappedBy = "classRoom")
    // private List<ActivityLog> activities;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @NotNull
    @Enumerated(EnumType.STRING) // Lưu bằng tên enum (AUTO, APPROVAL)
    @Column(name = "join_mode", nullable = false, length = 20)
    private JoinMode joinMode ; // default = AUTO

    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassSchedulePattern> schedulePatterns = new ArrayList<>();

    public enum JoinMode {
        AUTO,      // Học sinh vào không cần xác nhận
        APPROVAL   // Học sinh vào cần sự xác nhận của giáo viên
    }

}