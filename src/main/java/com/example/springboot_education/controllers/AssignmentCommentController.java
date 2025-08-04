package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.assignmentCommentDTOs.AssignmentCommentResponseDto;
import com.example.springboot_education.dtos.assignmentCommentDTOs.CreateAssignmentCommentRequestDto;
import com.example.springboot_education.services.assignment.AssignmentCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignment-comments")
@RequiredArgsConstructor
public class AssignmentCommentController {

    private final AssignmentCommentService assignmentCommentService;

    @PostMapping
    public ResponseEntity<AssignmentCommentResponseDto> createComment(
            @RequestBody CreateAssignmentCommentRequestDto dto) {
        return ResponseEntity.ok(assignmentCommentService.create(dto));
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<AssignmentCommentResponseDto>> getCommentsByAssignment(
            @PathVariable Integer assignmentId) {
        return ResponseEntity.ok(assignmentCommentService.getByAssignment(assignmentId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer id) {
        assignmentCommentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assignment/{assignmentId}/tree")
    public ResponseEntity<List<AssignmentCommentResponseDto>> getCommentsTreeByAssignment(
            @PathVariable Integer assignmentId
    ) {
        return ResponseEntity.ok(assignmentCommentService.getCommentsTreeByAssignment(assignmentId));
    }
}

