package com.example.springboot_education.dtos.assignmentCommentDTOs;


import lombok.*;

import java.sql.Timestamp;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class AssignmentCommentDTO {
    private Integer id;
    private Integer assignmentId;
    private Integer userId;
    private String  userName;    // tùy entity Users
    private String  avatarUrl;   // nếu có
    private String  comment;     // "[đã xoá]" nếu deleted
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private boolean edited;
    private boolean deleted;
    private Integer parentId;
    private Integer rootId;
    private int depth;
    private int childrenCount;
}
