package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.assignmentDTOs.AssignmentResponseDto;
import com.example.springboot_education.dtos.assignmentDTOs.CreateAssignmentRequestDto;
import com.example.springboot_education.dtos.assignmentDTOs.UpdateAssignmentRequestDto;
import com.example.springboot_education.dtos.materialDTOs.DownloadFileDTO;
import com.example.springboot_education.entities.Assignment;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.services.assignment.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping()
    public List<AssignmentResponseDto> getAllAssignments() {
        return assignmentService.getAllAssignments();
    }

    @GetMapping("/{id}")
    public AssignmentResponseDto getAssignmentById(@PathVariable("id") Integer id) {
        return assignmentService.getAssignmentById(id);
    }

    // @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // public ResponseEntity<AssignmentResponseDto> createAssignment(
    // @Valid @RequestParam("classId") Integer classId,
    // @Valid @RequestParam("title") String title,
    // @Valid @RequestParam(value = "description", required = false) String
    // description,
    // @Valid @RequestParam(value = "dueDate") @DateTimeFormat(iso =
    // DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
    // @Valid @RequestParam("maxScore") BigDecimal maxScore,
    // @RequestPart(value = "file", required = false) MultipartFile file) throws
    // IOException {
    // CreateAssignmentRequestDto dto = new CreateAssignmentRequestDto();
    // dto.setClassId(classId);
    // dto.setTitle(title);
    // dto.setDescription(description);
    // dto.setDueDate(dueDate);
    // dto.setMaxScore(maxScore);

    // return ResponseEntity.ok(assignmentService.createAssignmentWithFile(dto,
    // file));
    // }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentResponseDto> createAssignment(
            @Valid @ModelAttribute CreateAssignmentRequestDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(assignmentService.createAssignmentWithFile(dto, file));
    }

    // @PatchMapping(value = "/{id}", consumes =
    // MediaType.MULTIPART_FORM_DATA_VALUE)
    // public ResponseEntity<AssignmentResponseDto> updateAssignment(
    // @PathVariable("id") Integer id,
    // @Valid @RequestParam Integer classId,
    // @Valid @RequestParam String title,
    // @Valid @RequestParam(required = false) String description,
    // @Valid @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // LocalDateTime dueDate,
    // @Valid @RequestParam BigDecimal maxScore,
    // @RequestPart(value = "file", required = false) MultipartFile file) throws
    // IOException {
    // UpdateAssignmentRequestDto dto = new UpdateAssignmentRequestDto();
    // dto.setClassId(classId);
    // dto.setTitle(title);
    // dto.setDescription(description);
    // dto.setDueDate(dueDate);
    // dto.setMaxScore(maxScore);

    // AssignmentResponseDto updated = assignmentService.updateAssignment(id, dto,
    // file);
    // return ResponseEntity.ok(updated);
    // }
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentResponseDto> updateAssignment(
            @PathVariable Integer id,
            @Valid @ModelAttribute UpdateAssignmentRequestDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        AssignmentResponseDto updated = assignmentService.updateAssignment(id, dto, file);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteAssignment(@PathVariable("id") Integer id) {
        assignmentService.deleteAssignment(id);
    }

    @GetMapping("/{assignmentId}/download")
    public ResponseEntity<Resource> downloadAssignmentFile(@PathVariable("assignmentId") Integer assignmentId)
            throws IOException {
        Assignment assignment = assignmentService.getAssignmentEntityById(assignmentId);

        Path filePath = Paths.get(assignment.getFilePath());
        if (!Files.exists(filePath)) {
            throw new EntityNotFoundException("File");
        }

        Resource resource = new UrlResource(filePath.toUri());
        String fileName = filePath.getFileName().toString();

        // Encode tên file để tránh lỗi khi có dấu tiếng Việt
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<AssignmentResponseDto>> getAssignmentsByClassId(
            @PathVariable("classId") Integer classId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByClassId(classId));
    }

    // Tải file bài tập
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadAssignment(@PathVariable("id") Integer id) throws Exception {
        DownloadFileDTO fileDto = assignmentService.downloadAssignment(id);

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(fileDto.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.getFileName() + "\"")
                .body(fileDto.getResource());
    }

}
