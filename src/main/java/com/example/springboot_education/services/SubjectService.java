package com.example.springboot_education.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO;
import com.example.springboot_education.dtos.subjects.CreateSubjectDTO;
import com.example.springboot_education.dtos.subjects.SubjectResponseDTO;
import com.example.springboot_education.dtos.subjects.UpdateSubjectDTO;
import com.example.springboot_education.entities.Subject;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.SubjectRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final ActivityLogService activityLogService;

    private final UsersJpaRepository userRepository;

    public List<SubjectResponseDTO> findAll() {
        return subjectRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public SubjectResponseDTO findById(Integer id) {
        return subjectRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found with id: " + id));
    }

    public SubjectResponseDTO create(CreateSubjectDTO dto) {
        Subject subject = new Subject();
        subject.setSubjectName(dto.getSubjectName());
        subject.setDescription(dto.getDescription());

        Users creator = null;
        if (dto.getCreatedById() != null) {
            creator = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            subject.setCreatedBy(creator);
        }

        Subject saved = subjectRepository.save(subject);

        // Ghi log CREATE
        activityLogService.log(new ActivityLogCreateDTO(
                "CREATE",
                saved.getId(),
                "subjects",
                "Tạo môn học mới: " + saved.getSubjectName(),
                creator != null ? creator.getId() : null
        ));

        return toDTO(saved);
    }

    public SubjectResponseDTO update(Integer id, UpdateSubjectDTO dto) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found"));

        subject.setSubjectName(dto.getSubjectName());
        subject.setDescription(dto.getDescription());

        Subject updated = subjectRepository.save(subject);

        // Ghi log UPDATE
        activityLogService.log(new ActivityLogCreateDTO(
                "UPDATE",
                updated.getId(),
                "subjects",
                "Cập nhật môn học: " + updated.getSubjectName(),
                updated.getCreatedBy() != null ? updated.getCreatedBy().getId() : null
        ));

        return toDTO(updated);
    }

    public void delete(Integer id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found"));

        // Ghi log DELETE
        activityLogService.log(new ActivityLogCreateDTO(
                "DELETE",
                subject.getId(),
                "subjects",
                "Xóa môn học: " + subject.getSubjectName(),
                subject.getCreatedBy() != null ? subject.getCreatedBy().getId() : null
        ));

        subjectRepository.delete(subject);
    }

    private SubjectResponseDTO toDTO(Subject subject) {
        SubjectResponseDTO dto = new SubjectResponseDTO();
        dto.setId(subject.getId());
        dto.setSubjectName(subject.getSubjectName());
        dto.setDescription(subject.getDescription());
        dto.setCreatedByName(subject.getCreatedBy() != null ? subject.getCreatedBy().getFullName() : null);
        return dto;
    }
}
