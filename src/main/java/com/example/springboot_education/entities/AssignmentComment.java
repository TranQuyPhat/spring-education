package com.example.springboot_education.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
@Entity
@Table(
        name = "assignment_comments",
        indexes = {
                @Index(name = "ix_ac_assignment_parent_created", columnList = "assignment_id,parent_id,created_at"),
                @Index(name = "ix_ac_parent_created", columnList = "parent_id,created_at"),
                @Index(name = "ix_ac_assignment_created", columnList = "assignment_id,created_at"),
                @Index(name = "ix_ac_root_created", columnList = "root_id,created_at")
        }
)
public class AssignmentComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @NotNull
    @Lob
    @Column(name = "comment", nullable = false)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "edited", nullable = false)
    private boolean edited;

    @Column(name = "deleted_at")
    private Timestamp deletedAt; // soft delete

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "parent_id")
    private AssignmentComment parent;

    // id của comment gốc trong thread (root = chính nó)
    @Column(name = "root_id")
    private Integer rootId;

    // 0 = root, 1 = reply cấp 1...
    @Column(name = "depth", nullable = false)
    private int depth;

    // số reply trực tiếp
    @Column(name = "children_count", nullable = false)
    private int childrenCount;
}
