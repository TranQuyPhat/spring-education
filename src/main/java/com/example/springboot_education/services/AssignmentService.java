package com.example.springboot_education.services;

import com.example.springboot_education.dtos.assignmentDTOs.AssignmentResponseDto;
import com.example.springboot_education.dtos.assignmentDTOs.CreateAssignmentRequestDto;
import com.example.springboot_education.dtos.assignmentDTOs.UpdateAssignmentRequestDto;
import com.example.springboot_education.entities.Assignment;
import com.example.springboot_education.repositories.AssignmentJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AssignmentService {
    private AssignmentJpaRepository assignmentJpaRepository;

    public AssignmentService(AssignmentJpaRepository assignmentJpaRepository) {
        this.assignmentJpaRepository = assignmentJpaRepository;
    }

    private AssignmentResponseDto convertToDto(Assignment assignment) {
        return new AssignmentResponseDto(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getClassField().getId(), // assuming this method exists
                assignment.getDueDate(),
                assignment.getMaxScore(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt(),
                assignment.getFilePath(), // assuming this method exists
                assignment.getFileType() // assuming this method exists
        );
    }

    public List<AssignmentResponseDto> getAllAssignments() {
        List<Assignment> assignments = assignmentJpaRepository.findAll();
        return assignments.stream().map(this::convertToDto).toList();
    }

    public AssignmentResponseDto getAssignmentById(Long id) {
        Assignment assignment = assignmentJpaRepository.findById(id).orElseThrow();
        return convertToDto(assignment);
    }

    public AssignmentResponseDto createAssignment(CreateAssignmentRequestDto dto) {
        Assignment assignment = new Assignment();

        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setMaxScore(dto.getMaxScore());
        assignment.setCreatedAt(Instant.now());

        // Lấy class từ DB và set vào assignment
        // Class classEntity = classJpaRepository.findById(dto.getClass_id())
        //         .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + dto.getClass_id()));
        // assignment.setClassField(classEntity);

        Assignment savedAssignment = assignmentJpaRepository.save(assignment);
        return convertToDto(savedAssignment);
    }

    public AssignmentResponseDto updateAssignment(Long id, UpdateAssignmentRequestDto dto) {
        Assignment assignment = assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));

        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setMaxScore(dto.getMaxScore());
        assignment.setUpdatedAt(Instant.now());

        // Cập nhật class nếu có thay đổi
        // Class classEntity = classJpaRepository.findById(dto.getClass_id())
        //         .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + dto.getClass_id()));
        // assignment.setClassField(classEntity);

        Assignment updatedAssignment = assignmentJpaRepository.save(assignment);
        return convertToDto(updatedAssignment);
    }

    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentJpaRepository.findById(id).orElseThrow();
        assignmentJpaRepository.delete(assignment);
    }
}
