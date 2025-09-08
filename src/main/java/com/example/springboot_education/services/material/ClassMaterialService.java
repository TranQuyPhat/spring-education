package com.example.springboot_education.services.material;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassMaterialService {

    private final ClassMaterialJpaRepository classMaterialJpaRepository;
    private final UsersJpaRepository usersJpaRepository;
    private final ClassRepository classRepository;

    @LoggableAction(value = "CREATE", entity = "class_materials", description = "Created a new material")
    public ClassMaterialResponseDto createMaterial(ClassMaterialRequestDto dto, MultipartFile file) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Users user = usersJpaRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

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

    public List<ClassMaterialResponseDto> getAllMaterials() {
        return classMaterialJpaRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @LoggableAction(value = "UPDATE", entity = "class_materials", description = "Updated a material")
    public ClassMaterialResponseDto updateMaterial(Integer id, ClassMaterialRequestDto dto, MultipartFile file) throws IOException {
        ClassMaterial material = classMaterialJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        material.setTitle(dto.getTitle());
        material.setDescription(dto.getDescription());

        if (file != null && !file.isEmpty()) {
            String uploadDir = "uploads/documents";
            Files.createDirectories(Paths.get(uploadDir));

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, file.getBytes());

            material.setFilePath("uploads/documents/" + filename);
            material.setFileType(file.getContentType());
        }

        ClassMaterial updated = classMaterialJpaRepository.save(material);
        return toResponseDto(updated);
    }

    @LoggableAction(value = "DELETE", entity = "class_materials", description = "Deleted a material")
    public void deleteMaterial(Integer id) {
        ClassMaterial material = classMaterialJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));
        try {
            Path path = Paths.get(System.getProperty("user.dir")).resolve(material.getFilePath());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Could not delete file: " + e.getMessage());
        }

        classMaterialJpaRepository.delete(material);
    }

    public DownloadFileDTO downloadMaterial(Integer id) throws Exception {
        ClassMaterial material = classMaterialJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        material.setDownloadCount(material.getDownloadCount() + 1);
        classMaterialJpaRepository.save(material);

        Path path = Paths.get(System.getProperty("user.dir")).resolve(material.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("File not found: " + path.toAbsolutePath());
        }

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
