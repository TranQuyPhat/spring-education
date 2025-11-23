package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.assignmentDTOs.AssignmentResponseDto;
import com.example.springboot_education.dtos.assignmentDTOs.CreateAssignmentRequestDto;
import com.example.springboot_education.dtos.materialDTOs.DownloadFileDTO;
import com.example.springboot_education.dtos.submissionDTOs.GradeSubmissionRequestDto;
import com.example.springboot_education.dtos.submissionDTOs.SubmissionRequestDto;
import com.example.springboot_education.dtos.submissionDTOs.SubmissionResponseDto;
import com.example.springboot_education.entities.Submission;
import com.example.springboot_education.services.assignment.AssignmentService;
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

import java.io.IOException;
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
    // @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // public ResponseEntity<SubmissionResponseDto> submitAssignment(
    // @Valid @RequestParam("assignmentId") Integer assignmentId,
    // @Valid @RequestParam("studentId") Integer studentId,
    // @RequestPart(value = "file", required = false) MultipartFile file,
    // @Valid @RequestParam(required = false) String description) throws IOException
    // {

    // SubmissionRequestDto requestDto = new SubmissionRequestDto();
    // requestDto.setAssignmentId(assignmentId);
    // requestDto.setStudentId(studentId);
    // requestDto.setDescription(description);

    // return ResponseEntity.ok(submissionService.submitAssignment(requestDto,
    // file));
    // }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmissionResponseDto> submitAssignment(
            @Valid @ModelAttribute SubmissionRequestDto requestDto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        return ResponseEntity.ok(submissionService.submitAssignment(requestDto, file));
    }

    // Chỉnh sửa bài nộp
    @PatchMapping(value = "/{submissionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmissionResponseDto> updateSubmission(
            @PathVariable("submissionId") Integer submissionId,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        SubmissionRequestDto requestDto = new SubmissionRequestDto();
        requestDto.setDescription(description);

        SubmissionResponseDto updated = submissionService.updateSubmission(submissionId, requestDto, file);
        return ResponseEntity.ok(updated);
    }

    // Chấm điểm
    @PatchMapping("/{submissionId}/grade")
    public ResponseEntity<SubmissionResponseDto> gradeSubmission(
            @PathVariable("submissionId") Integer submissionId,
            @Valid @RequestBody GradeSubmissionRequestDto dto) {
        SubmissionResponseDto response = submissionService.gradeSubmission(
                submissionId, dto.getScore(), dto.getComment());
        return ResponseEntity.ok(response);
    }

    // Chỉnh sửa điểm
    @PatchMapping("/{submissionId}/update-grade")
    public ResponseEntity<SubmissionResponseDto> updateGradeSubmission(
            @PathVariable("submissionId") Integer submissionId,
            @Valid @RequestBody GradeSubmissionRequestDto dto) {
        SubmissionResponseDto response = submissionService.updateGradeSubmission(
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

    // Lấy bài nộp học sinh trong 1 lớp
    @GetMapping("/class/{classId}/student/{studentId}")
    public List<SubmissionResponseDto> getByClassAndStudent(
            @PathVariable Integer classId,
            @PathVariable Integer studentId) {
        return submissionService.getSubmissionsByClassIdAndStudentId(classId, studentId);
    }

    // Tải file nộp bài về
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadSubmission(@PathVariable("id") Integer id) throws Exception {
        DownloadFileDTO fileDto = submissionService.downloadSubmission(id);

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(fileDto.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.getFileName() + "\"")
                .body(fileDto.getResource());
    }

    // Xóa nộp bài
    @DeleteMapping("/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable("submissionId") Integer submissionId) {
        submissionService.deleteSubmission(submissionId);
        return ResponseEntity.noContent().build();
    }
}
