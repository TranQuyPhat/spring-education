package com.example.springboot_education.services.material;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.springboot_education.untils.CloudinaryUtils;
import org.springframework.stereotype.Service;

import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.materialDTOs.ClassMaterialRequestDto;
import com.example.springboot_education.dtos.materialDTOs.ClassMaterialResponseDto;
import com.example.springboot_education.dtos.materialDTOs.DownloadFileDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.ClassMaterial;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.material.ClassMaterialJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassMaterialService {

    private final Cloudinary cloudinary;
    private final ClassMaterialJpaRepository classMaterialJpaRepository;
    private final UsersJpaRepository usersJpaRepository;
    private final ClassRepository classRepository;

    @LoggableAction(value = "CREATE", entity = "class_materials", description = "Created a new material")
    public ClassMaterialResponseDto createMaterial(ClassMaterialRequestDto dto, MultipartFile file) throws IOException {
        Users user = usersJpaRepository.findById(dto.getCreatedBy())
                .orElseThrow(() -> new EntityNotFoundException("User" + dto.getCreatedBy()));

        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class with id: " + dto.getClassId()));

        // Upload lên Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "class_materials"));

        ClassMaterial material = new ClassMaterial();
        material.setTitle(dto.getTitle());
        material.setDescription(dto.getDescription());
        material.setCreatedBy(user);
        material.setClassField(classEntity);
        material.setFilePath((String) uploadResult.get("secure_url")); // URL public Cloudinary
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
                .orElseThrow(() -> new EntityNotFoundException("Material"));

        material.setTitle(dto.getTitle());
        material.setDescription(dto.getDescription());

        if (file != null && !file.isEmpty()) {
            // Nếu đã có file cũ thì xóa trên Cloudinary
            if (material.getFilePath() != null) {
                String publicId = extractPublicId(material.getFilePath());
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }

            // Upload file mới
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "class_materials"));

            material.setFilePath((String) uploadResult.get("secure_url"));
            material.setFileType(file.getContentType());
        }

        ClassMaterial updated = classMaterialJpaRepository.save(material);
        return toResponseDto(updated);
    }

    @LoggableAction(value = "DELETE", entity = "class_materials", description = "Deleted a material")
    public void deleteMaterial(Integer id) {
        ClassMaterial material = classMaterialJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Material"));

        // xóa file trên Cloudinary
        try {
            String publicId = CloudinaryUtils.extractPublicId(material.getFilePath());
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from Cloudinary: " + e.getMessage());
        }

        classMaterialJpaRepository.delete(material);
    }

    public String getDownloadUrl(Integer id) {
        ClassMaterial material = classMaterialJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Material"));

        material.setDownloadCount(material.getDownloadCount() + 1);
        classMaterialJpaRepository.save(material);

        return material.getFilePath(); // URL Cloudinary
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

    private String extractPublicId(String url) {
        // Ví dụ: https://res.cloudinary.com/djiinlgh2/image/upload/v1234567890/class_materials/abc123.pdf
        // => publicId = class_materials/abc123
        String withoutExtension = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
        String folder = url.split("/upload/")[1]; // lấy phần sau /upload/
        folder = folder.substring(0, folder.lastIndexOf("/")); // bỏ tên file
        return folder + "/" + withoutExtension;
    }

}