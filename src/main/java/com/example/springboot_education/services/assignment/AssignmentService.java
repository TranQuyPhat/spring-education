package com.example.springboot_education.services.assignment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.assignment.AssignmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.springboot_education.dtos.assignmentDTOs.AssignmentResponseDto;
import com.example.springboot_education.dtos.assignmentDTOs.CreateAssignmentRequestDto;
import com.example.springboot_education.dtos.assignmentDTOs.UpdateAssignmentRequestDto;
import com.example.springboot_education.entities.Assignment;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class AssignmentService {
    private final AssignmentJpaRepository assignmentJpaRepository;
    private final ClassRepository classRepository;

    private AssignmentResponseDto convertToDto(Assignment assignment) {
        AssignmentResponseDto assignmentResponseDto = new AssignmentResponseDto();

        assignmentResponseDto.setId(assignment.getId());
        assignmentResponseDto.setClassId(assignment.getClassField().getId());
        assignmentResponseDto.setTitle(assignment.getTitle());
        assignmentResponseDto.setDescription(assignment.getDescription());
        assignmentResponseDto.setDueDate(assignment.getDueDate());
        assignmentResponseDto.setMaxScore(assignment.getMaxScore());
        assignmentResponseDto.setFilePath(assignment.getFilePath());
        assignmentResponseDto.setFileType(assignment.getFileType());
        assignmentResponseDto.setCreatedAt(assignment.getCreatedAt());
        assignmentResponseDto.setUpdatedAt(assignment.getUpdatedAt());

        return assignmentResponseDto;
    }

    public List<AssignmentResponseDto> getAllAssignments() {
        List<Assignment> assignments = assignmentJpaRepository.findAll();
        return assignments.stream().map(this::convertToDto).toList();
    }

    public AssignmentResponseDto getAssignmentById(Integer id) {
        Assignment assignment = assignmentJpaRepository.findById(id).orElseThrow();
        return convertToDto(assignment);
    }

//    public AssignmentResponseDto createAssignment(CreateAssignmentRequestDto dto) {
//        Assignment assignment = new Assignment();
//
//        assignment.setTitle(dto.getTitle());
//        assignment.setClassId(dto.getClassId());
//        assignment.setDescription(dto.getDescription());
//        assignment.setDueDate(dto.getDueDate());
//        assignment.setMaxScore(dto.getMaxScore());
//        assignment.setFilePath(dto.getFilePath());
//        assignment.setFileType(dto.getFileType());
//
//        Assignment savedAssignment = assignmentJpaRepository.save(assignment);
//        return convertToDto(savedAssignment);
//    }

    //    Create assignment with file
    public AssignmentResponseDto createAssignmentWithFile(CreateAssignmentRequestDto dto, MultipartFile file) throws IOException {
        // 1. Kiểm tra class tồn tại
        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + dto.getClassId()));

        // 2. Upload file
        String uploadDir = "uploads/assignments";
        Files.createDirectories(Paths.get(uploadDir));

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        Files.write(filePath, file.getBytes());

        // 3. Tạo assignment
        Assignment assignment = new Assignment();
        assignment.setClassField(classEntity);
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setMaxScore(dto.getMaxScore());
        assignment.setFilePath(filePath.toString());
        assignment.setFileType(file.getContentType());

        Assignment saved = assignmentJpaRepository.save(assignment);
        return convertToDto(saved);
    }

    public AssignmentResponseDto updateAssignment(Integer id, UpdateAssignmentRequestDto dto) {
        Assignment assignment = assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + dto.getClassId()));

        assignment.setTitle(dto.getTitle());
        assignment.setClassField(classEntity);
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setMaxScore(dto.getMaxScore());
        assignment.setFilePath(dto.getFilePath());
        assignment.setFileType(dto.getFileType());


        Assignment updatedAssignment = assignmentJpaRepository.save(assignment);
        return convertToDto(updatedAssignment);
    }

    public void deleteAssignment(Integer id) {
        Assignment assignment = assignmentJpaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        assignmentJpaRepository.delete(assignment);
    }

    public Assignment getAssignmentEntityById(Integer id) {
        return assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
    }
}