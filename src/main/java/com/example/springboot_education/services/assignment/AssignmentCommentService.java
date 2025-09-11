package com.example.springboot_education.services.assignment;

import com.example.springboot_education.dtos.assignmentCommentDTOs.AssignmentCommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssignmentCommentService {
    Page<AssignmentCommentDTO> listRootComments(Integer assignmentId, Pageable pageable);
    Page<AssignmentCommentDTO> listReplies(Integer parentId, Pageable pageable);
    AssignmentCommentDTO createComment(Integer assignmentId, Integer userId, String text, Integer parentId);
    AssignmentCommentDTO editComment(Integer commentId, Integer userId, String newText);
    void deleteComment(Integer commentId, Integer userId); // soft-delete
}