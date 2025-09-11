package com.example.springboot_education.services.assignment.impl;


import com.example.springboot_education.dtos.assignmentCommentDTOs.AssignmentCommentDTO;
import com.example.springboot_education.dtos.assignmentCommentDTOs.CommentEvent;
import com.example.springboot_education.entities.Assignment;
import com.example.springboot_education.entities.AssignmentComment;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.assignment.AssignmentCommentRepository;
import com.example.springboot_education.repositories.assignment.AssignmentJpaRepository;
import com.example.springboot_education.services.assignment.AssignmentCommentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentCommentServiceImpl implements AssignmentCommentService {

    private final AssignmentJpaRepository assignmentRepo;
    private final UsersJpaRepository usersRepo;
    private final AssignmentCommentRepository commentRepo;

    private final SimpMessagingTemplate messaging;
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<AssignmentCommentDTO> listRootComments(Integer assignmentId, Pageable pageable) {
        Page<AssignmentComment> page = commentRepo
                .findByAssignmentIdAndParentIsNullOrderByCreatedAtDesc(assignmentId, pageable);

        return page.map(toDto());
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<AssignmentCommentDTO> listReplies(Integer parentId, Pageable pageable) {
        Page<AssignmentComment> page = commentRepo
                .findByParentIdOrderByCreatedAtDesc(parentId, pageable);

        return page.map(toDto());
    }


    @Override
    public AssignmentCommentDTO createComment(Integer assignmentId, Integer userId, String text, Integer parentId) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Comment content is required");
        }

        Assignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        AssignmentComment parent = null;
        Integer rootId = null;
        int depth = 0;

        if (parentId != null) {
            parent = commentRepo.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));

            if (!parent.getAssignment().getId().equals(assignmentId)) {
                throw new IllegalArgumentException("Parent comment belongs to another assignment");
            }
            depth = parent.getDepth() + 1;
            rootId = (parent.getRootId() != null) ? parent.getRootId() : parent.getId();
        }

        AssignmentComment c = new AssignmentComment();
        c.setAssignment(assignment);
        c.setUser(user);
        c.setComment(text);
        c.setParent(parent);
        c.setDepth(depth);
        c.setRootId(rootId);  // tạm thời null nếu root
        c.setEdited(false);
        c.setDeletedAt(null);
        c.setChildrenCount(0);

        commentRepo.save(c);

        // Nếu là root → rootId = chính nó
        if (parent == null) {
            c.setRootId(c.getId());
        } else {
            // tăng childrenCount cho parent
            parent.setChildrenCount(parent.getChildrenCount() + 1);
        }
        AssignmentCommentDTO dto = toDto().apply(c);
        CommentEvent ev = CommentEvent.builder()
                .type(CommentEvent.EventType.CREATED)
                .data(dto)
                .assignmentId(assignmentId)
                .rootId(dto.getRootId())
                .parentId(dto.getParentId())
                .build();

        // 1) Topic theo assignment: luôn bắn (để list roots cập nhật)
        messaging.convertAndSend(
                "/topic/assignments/" + assignmentId + "/comments",
                ev
        );

        // 2) Nếu là reply => bắn vào thread (rootId) để UI đang mở thread nhận ngay
        if (parent != null && dto.getRootId() != null) {
            messaging.convertAndSend("/topic/comments/" + dto.getRootId(), ev);
        }

        return dto;
    }

    @Override
    public AssignmentCommentDTO editComment(Integer commentId, Integer userId, String newText) {
        if (newText == null || newText.isBlank()) {
            throw new IllegalArgumentException("Comment content is required");
        }

        AssignmentComment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        // Quy tắc: chỉ author (hoặc teacher/admin tuỳ role) mới được sửa
        if (!c.getUser().getId().equals(userId)) {
            // ở dự án thật có thể check thêm role
            throw new SecurityException("You are not allowed to edit this comment");
        }

        // Nếu đã xoá mềm → không cho sửa
        if (c.getDeletedAt() != null) {
            throw new IllegalStateException("Comment is deleted");
        }

        c.setComment(newText);
        c.setEdited(true);
        AssignmentCommentDTO dto = toDto().apply(c);

        CommentEvent ev = CommentEvent.builder()
                .type(CommentEvent.EventType.UPDATED)
                .data(dto)
                .assignmentId(dto.getAssignmentId())
                .rootId(dto.getRootId())
                .parentId(dto.getParentId())
                .build();

        messaging.convertAndSend("/topic/assignments/" + dto.getAssignmentId() + "/comments", ev);
        if (dto.getRootId() != null) {
            messaging.convertAndSend("/topic/comments/" + dto.getRootId(), ev);
        }
        return dto;
    }

    @Override
    public void deleteComment(Integer commentId, Integer userId) {
        AssignmentComment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!c.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not allowed to delete this comment");
        }

        if (c.getDeletedAt() == null) {
            c.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        }

        AssignmentCommentDTO dto = toDto().apply(c);

        CommentEvent ev = CommentEvent.builder()
                .type(CommentEvent.EventType.DELETED)
                .data(dto)
                .assignmentId(dto.getAssignmentId())
                .rootId(dto.getRootId())
                .parentId(dto.getParentId())
                .build();

        messaging.convertAndSend("/topic/assignments/" + dto.getAssignmentId() + "/comments", ev);
        if (dto.getRootId() != null) {
            messaging.convertAndSend("/topic/comments/" + dto.getRootId(), ev);
        }
    }

    // ========= Mapper =========

    private Function<AssignmentComment, AssignmentCommentDTO> toDto() {
        return c -> {
            boolean deleted = c.getDeletedAt() != null;

            return AssignmentCommentDTO.builder()
                    .id(c.getId())
                    .assignmentId(c.getAssignment().getId())
                    .userId(c.getUser().getId())
                    .userName(safeUserName(c))       // tuỳ entity Users
                    .avatarUrl(safeAvatar(c))        // tuỳ entity Users
                    .comment(deleted ? "[đã xoá]" : c.getComment())
                    .createdAt(c.getCreatedAt())
                    .updatedAt(c.getUpdatedAt())
                    .edited(c.isEdited())
                    .deleted(deleted)
                    .parentId(c.getParent() != null ? c.getParent().getId() : null)
                    .rootId(c.getRootId())
                    .depth(c.getDepth())
                    .childrenCount(c.getChildrenCount())
                    .build();
        };
    }

    private String safeUserName(AssignmentComment c) {
        try { return c.getUser().getFullName(); } catch (Exception e) { return null; }
    }

    private String safeAvatar(AssignmentComment c) {
        try { return c.getUser().getAvatar().toString(); } catch (Exception e) { return null; }
    }
}