package com.example.springboot_education.services.material;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.materialDTOs.ClassMaterialRequestDto;
import com.example.springboot_education.dtos.materialDTOs.ClassMaterialResponseDto;
import com.example.springboot_education.dtos.materialDTOs.DownloadFileDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.ClassMaterial;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.material.ClassMaterialJpaRepository;
import com.example.springboot_education.services.ActivityLogService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassMaterialService {

    private final ClassMaterialJpaRepository classMaterialJpaRepository;
    private final UsersJpaRepository usersJpaRepository;
    private final ClassRepository classRepository;
    private final ActivityLogService activityLogService;

    public ClassMaterialResponseDto createMaterial(ClassMaterialRequestDto dto, MultipartFile file) throws IOException {
   
    @LoggableAction(value = "CREATE", entity = "class_materials", description = "Tạo tài liệu mới")
    public ClassMaterialResponseDto createMaterial(ClassMaterialRequestDto dto) {
        Users user = usersJpaRepository.findById(dto.getCreatedBy())
                .orElseThrow(() -> new EntityNotFoundException("User not found" + dto.getCreatedBy()));

        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + dto.getClassId()));

        ClassMaterial material = new ClassMaterial();
        material.setTitle(dto.getTitle());
        material.setDescription(dto.getDescription());
        material.setCreatedBy(user);
        material.setClassField(classEntity);

        String uploadDir = "uploads/documents";
        Files.createDirectories(Paths.get(uploadDir));

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        Files.write(filePath, file.getBytes());

        material.setFilePath("uploads/documents/" + filename);
        material.setFileType(file.getContentType());

        ClassMaterial saved = classMaterialJpaRepository.save(material);

        return toResponseDto(saved);
    }

    public List<ClassMaterialResponseDto> getMaterialsByClass(Integer classId) {
        return classMaterialJpaRepository.findByClassField_Id(classId)
                .stream().map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @LoggableAction(value = "UPDATE", entity = "class_materials", description = "Cập nhật tài liệu")
    public ClassMaterialResponseDto updateMaterial(Integer id, ClassMaterialRequestDto dto) {
        ClassMaterial material = classMaterialJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        material.setTitle(dto.getTitle());
        material.setDescription(dto.getDescription());
        material.setFilePath(dto.getFilePath());
        material.setFileType(dto.getFileType());

        ClassMaterial updated = classMaterialJpaRepository.save(material);
        return toResponseDto(updated);
    }

    @LoggableAction(value = "DELETE", entity = "class_materials", description = "Xóa tài liệu")
    public void deleteMaterial(Integer id) {
        ClassMaterial material = classMaterialJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        classMaterialJpaRepository.delete(material);
    }

    public void increaseDownloadCount(Integer materialId) {
        ClassMaterial material = classMaterialJpaRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));
        material.setDownloadCount(material.getDownloadCount() + 1);
        classMaterialJpaRepository.save(material);
    }

    public DownloadFileDTO downloadMaterial(Integer id) throws Exception {
        // 1. Lấy thông tin tài liệu
        ClassMaterial material = classMaterialJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        // 2. Tăng lượt tải
        material.setDownloadCount(material.getDownloadCount() + 1);
        classMaterialJpaRepository.save(material);

        // 3. Lấy file từ đường dẫn (dùng path tuyệt đối)
        Path path = Paths.get(material.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("File not found");
        }

        // 4. Trả DTO chứa file và metadata
        return new DownloadFileDTO(
                resource,
                material.getFileType() != null ? material.getFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE,
                path.getFileName().toString()
        );
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