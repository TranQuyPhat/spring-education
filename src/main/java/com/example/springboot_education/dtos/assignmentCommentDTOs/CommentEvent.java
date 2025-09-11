package com.example.springboot_education.dtos.assignmentCommentDTOs;
import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class CommentEvent {
    public enum EventType { CREATED, UPDATED, DELETED }

    private EventType type;
    private AssignmentCommentDTO data;
    private Integer assignmentId;
    private Integer rootId;
    private Integer parentId;
}