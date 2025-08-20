package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.submissionDTOs.GradeSubmissionRequestDto;
import com.example.springboot_education.dtos.submissionDTOs.SubmissionRequestDto;
import com.example.springboot_education.dtos.submissionDTOs.SubmissionResponseDto;
import com.example.springboot_education.entities.Submission;
import com.example.springboot_education.services.assignment.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService submissionService;

    @GetMapping
    public List<SubmissionResponseDto> getAllSubmissions() {
        return submissionService.getAllSubmissions();
    }

    // Upload và nộp bài
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmissionResponseDto> submitAssignment(
            @RequestParam("assignmentId") Integer assignmentId,
            @RequestParam("studentId") Integer studentId,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        SubmissionRequestDto requestDto = new SubmissionRequestDto();
        requestDto.setAssignmentId(assignmentId);
        requestDto.setStudentId(studentId);

//        SubmissionResponseDto response = submissionService.submitAssignment(requestDto);
        return ResponseEntity.ok(submissionService.submitAssignment(requestDto, file));
    }

    // Chấm điểm
    @PatchMapping("/{submissionId}/grade")
    public ResponseEntity<SubmissionResponseDto> gradeSubmission(
            @PathVariable Integer submissionId,
            @RequestBody @Valid GradeSubmissionRequestDto dto) {
        SubmissionResponseDto response = submissionService.gradeSubmission(
                submissionId, dto.getScore(), dto.getComment());
        return ResponseEntity.ok(response);
    }

    // Lấy bài nộp theo assignment
    @GetMapping("/assignment/{assignmentId}")
    public List<SubmissionResponseDto> getByAssignment(@PathVariable("assignmentId") Integer assignmentId) {
        return submissionService.getSubmissionsByAssignment(assignmentId);
    }

    // Lấy bài nộp theo học sinh
    @GetMapping("/student/{studentId}")
    public List<SubmissionResponseDto> getByStudent(@PathVariable Integer studentId) {
        return submissionService.getSubmissionsByStudent(studentId);
    }

    // Lấy bài nộp duy nhất của học sinh cho 1 assignment
    @GetMapping("/assignment/{assignmentId}/student/{studentId}")
    public SubmissionResponseDto getByAssignmentAndStudent(
            @PathVariable Integer assignmentId,
            @PathVariable Integer studentId) {
        return submissionService.getSubmission(assignmentId, studentId);
    }

    // Lấy bài nộp theo lớp
    @GetMapping("/class/{classId}")
    public List<SubmissionResponseDto> getAssignmentsByClassId(@PathVariable Integer classId) {
        return submissionService.getSubmissionsByClassId(classId);
    }

    // Tải file nộp bài về
    @GetMapping("/{submissionId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Integer submissionId) throws IOException {
        Submission submission = submissionService.getSubmissionEntityById(submissionId);

        Path filePath = Paths.get(submission.getFilePath());
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found");
        }

        Resource fileResource = new UrlResource(filePath.toUri());
        String fileName = filePath.getFileName().toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(fileResource);
    }

    // Xóa nộp bài
    @DeleteMapping("/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Integer submissionId) {
        submissionService.deleteSubmission(submissionId);
        return ResponseEntity.noContent().build();
    }
}



