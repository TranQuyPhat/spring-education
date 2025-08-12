package com.example.springboot_education.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

// Xóa import ActivityLogCreateDTO
// import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO; 
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

    

    private SubjectResponseDTO toDTO(Subject subject) {
        SubjectResponseDTO dto = new SubjectResponseDTO();
        dto.setId(subject.getId());
        dto.setSubjectName(subject.getSubjectName());
        dto.setDescription(subject.getDescription());
        dto.setCreatedByName(subject.getCreatedBy() != null ? subject.getCreatedBy().getFullName() : null);
        return dto;
    }
}