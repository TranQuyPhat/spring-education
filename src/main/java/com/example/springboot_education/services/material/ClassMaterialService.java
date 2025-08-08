package com.example.springboot_education.services.material;

import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO;
import com.example.springboot_education.dtos.materialDTOs.ClassMaterialRequestDto;
import com.example.springboot_education.dtos.materialDTOs.ClassMaterialResponseDto;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.ClassMaterial;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.material.ClassMaterialJpaRepository;
import com.example.springboot_education.services.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassMaterialService {

    private final ClassMaterialJpaRepository classMaterialJpaRepository;
    private final UsersJpaRepository usersJpaRepository;
    private final ActivityLogService activityLogService;
    private final ClassRepository classRepository;

    public ClassMaterialResponseDto createMaterial(ClassMaterialRequestDto dto) {
        Users user = usersJpaRepository.findById(dto.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        ClassMaterial material = new ClassMaterial();
        material.setTitle(dto.getTitle());
        material.setDescription(dto.getDescription());
        material.setFilePath(dto.getFilePath());
        material.setFileType(dto.getFileType());
        material.setCreatedBy(user);
        material.setClassField(classEntity);

        ClassMaterial saved = classMaterialJpaRepository.save(material);

        // Ghi log CREATE
        activityLogService.log(new ActivityLogCreateDTO(
                "CREATE",
                saved.getId(),
                "class_materials",
                "Tạo tài liệu: " + saved.getTitle(),
                user.getId()
        ));

        return toResponseDto(saved);
    }

    public List<ClassMaterialResponseDto> getMaterialsByClass(Integer classId) {
        return classMaterialJpaRepository.findByClassField_Id(classId)
                .stream().map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public ClassMaterialResponseDto updateMaterial(Integer id, ClassMaterialRequestDto dto) {
        ClassMaterial material = classMaterialJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        material.setTitle(dto.getTitle());
        material.setDescription(dto.getDescription());
        material.setFilePath(dto.getFilePath());
        material.setFileType(dto.getFileType());

        ClassMaterial updated = classMaterialJpaRepository.save(material);

        // Ghi log UPDATE
        activityLogService.log(new ActivityLogCreateDTO(
                "UPDATE",
                updated.getId(),
                "class_materials",
                "Cập nhật tài liệu: " + updated.getTitle(),
                updated.getCreatedBy() != null ? updated.getCreatedBy().getId() : null
        ));

        return toResponseDto(updated);
    }

    public void deleteMaterial(Integer id) {
        ClassMaterial material = classMaterialJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        // Ghi log DELETE
        activityLogService.log(new ActivityLogCreateDTO(
                "DELETE",
                material.getId(),
                "class_materials",
                "Xóa tài liệu: " + material.getTitle(),
                material.getCreatedBy() != null ? material.getCreatedBy().getId() : null
        ));

        classMaterialJpaRepository.delete(material);
    }

    public void increaseDownloadCount(Integer materialId) {
        ClassMaterial material = classMaterialJpaRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));
        material.setDownloadCount(material.getDownloadCount() + 1);
        classMaterialJpaRepository.save(material);
    }

    private ClassMaterialResponseDto toResponseDto(ClassMaterial material) {
        ClassMaterialResponseDto dto = new ClassMaterialResponseDto();
        dto.setId(material.getId());
        dto.setTitle(material.getTitle());
        dto.setDescription(material.getDescription());
        dto.setFilePath(material.getFilePath());
        dto.setFileType(material.getFileType());
        dto.setCreatedBy(material.getCreatedBy().getFullName());
        dto.setClassId(material.getClassField().getId());
        dto.setDownloadCount(material.getDownloadCount());
        dto.setCreatedAt(material.getCreatedAt());
        dto.setUpdatedAt(material.getUpdatedAt());
        return dto;
    }
}
