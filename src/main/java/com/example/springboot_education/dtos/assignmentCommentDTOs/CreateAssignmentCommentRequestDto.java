package com.example.springboot_education.dtos.assignmentCommentDTOs;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CreateAssignmentCommentRequestDto {
    private Integer assignmentId;
    private Integer userId;
    private String comment;
    private Integer parentId; // optional
}
