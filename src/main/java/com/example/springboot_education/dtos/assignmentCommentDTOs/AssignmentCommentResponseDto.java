package com.example.springboot_education.dtos.assignmentCommentDTOs;

import lombok.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AssignmentCommentResponseDto {
    private Integer id;
    private Integer assignmentId;
    private Integer userId;
    private String comment;
    private Timestamp createdAt;
    private Integer parentId;

    @Builder.Default
    private List<AssignmentCommentResponseDto> replies = new ArrayList<>();
}