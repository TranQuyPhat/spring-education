package com.example.springboot_education.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.springboot_education.annotations.LoggableAction;
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

    @LoggableAction(value = "CREATE", entity = "subjects", description = "Create new subject")
    public SubjectResponseDTO create(CreateSubjectDTO dto) {
        Subject subject = new Subject();
        subject.setSubjectName(dto.getSubjectName());
        subject.setDescription(dto.getDescription());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Users creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        subject.setCreatedBy(creator);

        Subject saved = subjectRepository.save(subject);
        return toDTO(saved);
    }

    @LoggableAction(value = "UPDATE", entity = "subjects", description = "Update subject")
    public SubjectResponseDTO update(Integer id, UpdateSubjectDTO dto) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found"));

        subject.setSubjectName(dto.getSubjectName());
        subject.setDescription(dto.getDescription());

        Subject updated = subjectRepository.save(subject);

        return toDTO(updated);
    }

    @LoggableAction(value = "DELETE", entity = "subjects", description = "Delete subject")
    public void delete(Integer id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found"));

        subjectRepository.delete(subject);
    }

    private SubjectResponseDTO toDTO(Subject subject) {
        SubjectResponseDTO dto = new SubjectResponseDTO();
        dto.setId(subject.getId());
        dto.setSubjectName(subject.getSubjectName());
        dto.setDescription(subject.getDescription());
        dto.setCreatedByName(subject.getCreatedBy() != null ? subject.getCreatedBy().getFullName() : null);
        dto.setCreatedAt(subject.getCreatedAt());
        dto.setUpdatedAt(subject.getUpdatedAt());

        return dto;
    }
}
