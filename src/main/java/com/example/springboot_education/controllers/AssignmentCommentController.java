package com.example.springboot_education.controllers;

import com.example.springboot_education.annotations.CurrentUser;
import com.example.springboot_education.dtos.assignmentCommentDTOs.AssignmentCommentDTO;
import com.example.springboot_education.dtos.assignmentCommentDTOs.CreateAssignmentCommentRequestDto;
import com.example.springboot_education.dtos.assignmentCommentDTOs.EditAssignmentCommentRequestDto;
import com.example.springboot_education.dtos.quiz.APIResponse;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.services.assignment.AssignmentCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignment-comments")
@RequiredArgsConstructor
public class AssignmentCommentController {

    private final AssignmentCommentService service;

    @GetMapping("/assignment/{assignmentId}/roots")
    public ResponseEntity<APIResponse<Page<AssignmentCommentDTO>>> listRoots(
            @PathVariable Integer assignmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AssignmentCommentDTO> pageResult = service.listRootComments(assignmentId, pageable);
        return ResponseEntity.ok(new APIResponse<>(true, "Lấy comment thành công", pageResult));
    }

    @GetMapping("/{parentId}/replies")
    public ResponseEntity<APIResponse<Page<AssignmentCommentDTO>>> listReplies(
            @PathVariable Integer parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AssignmentCommentDTO> pageResult = service.listReplies(parentId, pageable);
        return ResponseEntity.ok(new APIResponse<>(true, "Lấy reply thành công", pageResult));
    }

    @PostMapping
    public ResponseEntity<APIResponse<AssignmentCommentDTO>> create(
            @RequestBody CreateAssignmentCommentRequestDto dto,
            @CurrentUser Users currentUser
    ) {
        Integer userId = currentUser.getId();
        AssignmentCommentDTO created = service.createComment(
                dto.getAssignmentId(), userId, dto.getComment(), dto.getParentId()
        );
        return ResponseEntity.ok(new APIResponse<>(true, "Tạo comment thành công", created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<APIResponse<AssignmentCommentDTO>> edit(
            @PathVariable Integer id,
            @RequestBody EditAssignmentCommentRequestDto dto,
            @AuthenticationPrincipal Users me
    ) {
        AssignmentCommentDTO updated = service.editComment(id, me.getId(), dto.getComment());
        return ResponseEntity.ok(new APIResponse<>(true, "Sửa comment thành công", updated));
    }   
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal Users me
    ) {
        service.deleteComment(id, me.getId());
        return ResponseEntity.ok(new APIResponse<>(true, "Xoá comment thành công", null));
    }
}
