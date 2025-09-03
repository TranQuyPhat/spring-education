package com.example.springboot_education.dtos.assignmentCommentDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CreateAssignmentCommentRequestDto {
    @NotNull(message = "Assignment ID is required")
    private Integer assignmentId;

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotBlank(message = "Comment is required")
    private String comment;

    private Integer parentId;// optional
}
