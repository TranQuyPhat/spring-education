package com.example.springboot_education.services.assignment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.springboot_education.annotations.LoggableAction; // Import annotation
import com.example.springboot_education.dtos.assignmentDTOs.AssignmentResponseDto;
import com.example.springboot_education.dtos.assignmentDTOs.CreateAssignmentRequestDto;
import com.example.springboot_education.dtos.assignmentDTOs.UpdateAssignmentRequestDto;
import com.example.springboot_education.entities.Assignment;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.assignment.AssignmentJpaRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AssignmentService {

    private final AssignmentJpaRepository assignmentJpaRepository;
    private final ClassRepository classRepository;
    // Xóa ActivityLogService khỏi đây
    // private final ActivityLogService activityLogService;

    private AssignmentResponseDto convertToDto(Assignment assignment) {
        AssignmentResponseDto dto = new AssignmentResponseDto();
        dto.setId(assignment.getId());
        dto.setClassId(assignment.getClassField().getId());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setDueDate(assignment.getDueDate());
        dto.setMaxScore(assignment.getMaxScore());
        dto.setFilePath(assignment.getFilePath());
        dto.setFileType(assignment.getFileType());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setUpdatedAt(assignment.getUpdatedAt());
        return dto;
    }

    public List<AssignmentResponseDto> getAllAssignments() {
        return assignmentJpaRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public AssignmentResponseDto getAssignmentById(Integer id) {
        Assignment assignment = assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        return convertToDto(assignment);
    }

    @LoggableAction(value = "CREATE", entity = "assignments", description = "Tạo bài tập mới")
    public AssignmentResponseDto createAssignmentWithFile(CreateAssignmentRequestDto dto, MultipartFile file) throws IOException {
        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + dto.getClassId()));

        String uploadDir = "uploads/assignments";
        Files.createDirectories(Paths.get(uploadDir));

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        Files.write(filePath, file.getBytes());

        Assignment assignment = new Assignment();
        assignment.setClassField(classEntity);
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setMaxScore(dto.getMaxScore());
        assignment.setFilePath(filePath.toString());
        assignment.setFileType(file.getContentType());

        Assignment saved = assignmentJpaRepository.save(assignment);
        
        // Xóa code ghi log thủ công
        // activityLogService.log(...);

        return convertToDto(saved);
    }

    @LoggableAction(value = "UPDATE", entity = "assignments", description = "Cập nhật bài tập")
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

        Assignment updated = assignmentJpaRepository.save(assignment);

        // Xóa code ghi log thủ công
        // activityLogService.log(...);

        return convertToDto(updated);
    }

    @LoggableAction(value = "DELETE", entity = "assignments", description = "Xóa bài tập")
    public void deleteAssignment(Integer id) {
        Assignment assignment = assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));

        // Xóa code ghi log thủ công
        // activityLogService.log(...);

        assignmentJpaRepository.delete(assignment);
    }

    public Assignment getAssignmentEntityById(Integer id) {
        return assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
    }

    // Get assignment by class
    public List<AssignmentResponseDto> getAssignmentsByClassId(Integer classId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + classId));

        return assignmentJpaRepository.findByClassField_Id(classId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

}

