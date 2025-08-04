package com.example.springboot_education.services.assignment;

import com.example.springboot_education.dtos.assignmentCommentDTOs.AssignmentCommentResponseDto;
import com.example.springboot_education.dtos.assignmentCommentDTOs.CreateAssignmentCommentRequestDto;
import com.example.springboot_education.entities.Assignment;
import com.example.springboot_education.entities.AssignmentComment;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.assignment.AssignmentCommentJpaRepository;
import com.example.springboot_education.repositories.assignment.AssignmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssignmentCommentService {

    private final AssignmentCommentJpaRepository assignmentCommentJpaRepository;
    private final AssignmentJpaRepository assignmentJpaRepository;
    private final UsersJpaRepository usersJpaRepository;

    public AssignmentCommentResponseDto create(CreateAssignmentCommentRequestDto dto) {
        Assignment assignment = assignmentJpaRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        Users user = usersJpaRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AssignmentComment comment = AssignmentComment.builder()
                .assignment(assignment)
                .user(user)
                .comment(dto.getComment())
                .build();

        if (dto.getParentId() != null) {
            AssignmentComment parent = assignmentCommentJpaRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParent(parent);
        }

        AssignmentComment saved = assignmentCommentJpaRepository.save(comment);
        return toDto(saved);
    }

    public List<AssignmentCommentResponseDto> getByAssignment(Integer assignmentId) {
        return assignmentCommentJpaRepository.findByAssignmentId(assignmentId).stream()
                .map(this::toDto)
                .toList();
    }

    public void delete(Integer id) {
        assignmentCommentJpaRepository.deleteById(id);
    }

    private AssignmentCommentResponseDto toDto(AssignmentComment comment) {
        AssignmentCommentResponseDto dto = new AssignmentCommentResponseDto();
        dto.setId(comment.getId());
        dto.setAssignmentId(comment.getAssignment().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setComment(comment.getComment());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        return dto;
    }

    public List<AssignmentCommentResponseDto> getCommentsTreeByAssignment(Integer assignmentId) {
        List<AssignmentComment> all = assignmentCommentJpaRepository.findByAssignmentId(assignmentId);

        // Convert to Map by ID
        Map<Integer, AssignmentCommentResponseDto> dtoMap = new HashMap<>();

        for (AssignmentComment c : all) {
            AssignmentCommentResponseDto dto = new AssignmentCommentResponseDto();
            dto.setId(c.getId());
            dto.setAssignmentId(c.getAssignment().getId());
            dto.setUserId(c.getUser().getId());
            dto.setComment(c.getComment());
            dto.setCreatedAt(c.getCreatedAt());
            dto.setParentId(c.getParent() != null ? c.getParent().getId() : null);
            dtoMap.put(dto.getId(), dto);
        }

        // Build tree
        List<AssignmentCommentResponseDto> roots = new ArrayList<>();

        for (AssignmentCommentResponseDto dto : dtoMap.values()) {
            if (dto.getParentId() == null) {
                roots.add(dto);
            } else {
                AssignmentCommentResponseDto parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getReplies().add(dto);
                }
            }
        }

        return roots;
    }

}
