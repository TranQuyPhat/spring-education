package com.example.springboot_education.services.assignment;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.materialDTOs.DownloadFileDTO;
import com.example.springboot_education.dtos.notification.NotificationTeacherDTO;
import com.example.springboot_education.dtos.submissionDTOs.SubmissionRequestDto;
import com.example.springboot_education.dtos.submissionDTOs.SubmissionResponseDto;
import com.example.springboot_education.entities.Assignment;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.Submission;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.repositories.assignment.AssignmentJpaRepository;
import com.example.springboot_education.repositories.assignment.SubmissionJpaRepository;
import com.example.springboot_education.untils.CloudinaryUtils;
import com.example.springboot_education.services.SlackService;
import com.example.springboot_education.services.classes.NotificationService;
import com.example.springboot_education.untils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot_education.services.mail.EmailService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionJpaRepository submissionJpaRepository;
    private final AssignmentJpaRepository assignmentJpaRepository;
    private final UsersJpaRepository usersJpaRepository;
    private final ClassRepository classRepository;
    private final Cloudinary cloudinary;
    private final SlackService slackService;
    private final NotificationService notificationService;

    private SubmissionResponseDto convertToDto(Submission submission) {
        SubmissionResponseDto dto = new SubmissionResponseDto();
        dto.setId(submission.getId());
        dto.setAssignmentId(submission.getAssignment().getId());
        dto.setFilePath(submission.getFilePath());
        dto.setFileType(submission.getFileType());
        dto.setFileSize(FileUtils.formatFileSize(submission.getFileSize()));
        dto.setFileName(submission.getFileName());
        dto.setDescription(submission.getDescription());
        dto.setStatus(submission.getStatus());
        dto.setScore(submission.getScore());
        dto.setTeacherComment(submission.getTeacherComment());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setGradedAt(submission.getGradedAt());

        if (submission.getStudent() != null) {
            SubmissionResponseDto.StudentDto studentDto = SubmissionResponseDto.StudentDto.builder()
                    .id(submission.getStudent().getId())
                    .fullName(submission.getStudent().getFullName())
                    .email(submission.getStudent().getEmail())
                    .avatarBase64(
                            submission.getStudent().getAvatar() != null
                                    ? "data:image/png;base64,"
                                            + Base64.getEncoder().encodeToString(submission.getStudent().getAvatar())
                                    : null)
                    .build();

            dto.setStudent(studentDto);
        }

        if (submission.getAssignment() != null) {
            SubmissionResponseDto.AssignmentDto assignmentDto = SubmissionResponseDto.AssignmentDto.builder()
                    .id(submission.getAssignment().getId())
                    .title(submission.getAssignment().getTitle())
                    .published(submission.getAssignment().isPublished())
                    .build();

            dto.setAssignment(assignmentDto);
        }

        return dto;
    }

    public List<SubmissionResponseDto> getAllSubmissions() {
        List<Submission> submissions = submissionJpaRepository.findAll();
        return submissions.stream().map(this::convertToDto).toList();
    }

    // Nộp bài
    @LoggableAction(value = "SUBMIT", entity = "Submission", description = "Submitted an assignment")
    public SubmissionResponseDto submitAssignment(SubmissionRequestDto requestDto, MultipartFile file)
            throws IOException {
        Assignment assignment = assignmentJpaRepository.findById(requestDto.getAssignmentId())
                .orElseThrow(() -> new EntityNotFoundException("Assignment"));

        // Kiểm tra hạn nộp
        LocalDateTime now = LocalDateTime.now();
        if (assignment.getDueDate() != null && now.isAfter(assignment.getDueDate())) {
            throw new HttpException("Submission deadline has passed", HttpStatus.BAD_REQUEST);
        }

        Users student = usersJpaRepository.findById(requestDto.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Student"));

        submissionJpaRepository.findByAssignmentIdAndStudentId(
                requestDto.getAssignmentId(), requestDto.getStudentId()).ifPresent(existing -> {
                    throw new HttpException("You have already submitted this assignment", HttpStatus.CONFLICT);
                });

        Map<String, Object> uploadResult;
        try {
            uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", "submissions/" + assignment.getId() // gom theo assignment
                    ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload submission file to Cloudinary: " + e.getMessage(), e);
        }
        String fileUrl = (String) uploadResult.get("secure_url");
        String originalFilename = file.getOriginalFilename();

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFilePath(fileUrl);
        submission.setFileType(file.getContentType());
        submission.setFileSize(file.getSize());
        submission.setFileName(originalFilename);
        submission.setDescription(requestDto.getDescription());
        submission.setStatus(Submission.SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(now);
        
        Submission saved = submissionJpaRepository.save(submission);

        Map<String, Object> payload = Map.of(
                "student", student.getFullName(),
                "title", submission.getAssignment().getTitle());
        slackService.sendSlackNotification(
                assignment.getClassField().getId(),
                SlackService.ClassEventType.ASSIGNMENT_SUBMITTED,
                payload);
        NotificationTeacherDTO notifyPayload = NotificationTeacherDTO.builder()
                .classId(assignment.getClassField().getId())
                .studentName(student.getFullName())
                .message("Có học sinh nộp bài tập: " + assignment.getTitle())
                .build();
        System.out.println(
                "Notifying class ID: " + assignment.getClassField().getId() + " with payload: " + notifyPayload);

        notificationService.notifyTeacher(assignment.getClassField().getTeacher().getId(), notifyPayload);
        return convertToDto(saved);
    }

    // Chỉnh sửa bài nộp (chỉ cho phép khi chưa chấm điểm)
    public SubmissionResponseDto updateSubmission(Integer submissionId, SubmissionRequestDto requestDto,
            MultipartFile newFile) throws IOException {
        Submission submission = submissionJpaRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission"));

        // Không cho chỉnh sửa nếu đã chấm
        if (submission.getStatus() == Submission.SubmissionStatus.GRADED) {
            throw new HttpException("Bài nộp đã được chấm, không thể chỉnh sửa!", HttpStatus.FORBIDDEN);
        }

        // Nếu có file mới thì thay thế file cũ
        if (newFile != null && !newFile.isEmpty()) {
            // Xóa file cũ trên Cloudinary (nếu có)
            if (submission.getFilePath() != null && !submission.getFilePath().isBlank()) {
                try {
                    String publicId = CloudinaryUtils.extractPublicId(submission.getFilePath());
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                } catch (Exception e) {
                    log.warn("Không thể xóa file cũ trên Cloudinary: {}", submission.getFilePath(), e);
                }
            }

            // Upload file mới lên Cloudinary
            try {
                Map<String, Object> uploadResult = cloudinary.uploader().upload(
                        newFile.getBytes(),
                        ObjectUtils.asMap(
                                "resource_type", "auto",
                                "folder", "submissions/" + submission.getAssignment().getId()));

                submission.setFilePath(uploadResult.get("secure_url").toString());
                submission.setFileType(newFile.getContentType());
                submission.setFileSize(newFile.getSize());
                submission.setFileName(newFile.getOriginalFilename());
            } catch (Exception e) {
                throw new RuntimeException("Không thể upload file mới lên Cloudinary: " + e.getMessage(), e);
            }
        }

        // Cập nhật mô tả nếu có
        if (requestDto.getDescription() != null) {
            submission.setDescription(requestDto.getDescription());
        }

        // Cập nhật lại thời gian nộp
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(Submission.SubmissionStatus.SUBMITTED);

        Submission updated = submissionJpaRepository.save(submission);
        return convertToDto(updated);
    }

    // Chấm điểm
    @LoggableAction(value = "GRADE", entity = "Submission", description = "Graded an assignment")
    public SubmissionResponseDto gradeSubmission(Integer submissionId, BigDecimal score, String comment) {
        Submission submission = submissionJpaRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission"));

        submission.setScore(score);
        submission.setTeacherComment(comment);
        submission.setStatus(Submission.SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());

        Submission graded = submissionJpaRepository.save(submission);
        return convertToDto(graded);
    }

    // Chỉnh sửa điểm
    public SubmissionResponseDto updateGradeSubmission(Integer submissionId, BigDecimal score, String comment) {
        Submission submission = submissionJpaRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission"));

        if (submission.getStatus() != Submission.SubmissionStatus.GRADED) {
            throw new HttpException("Bài nộp chưa được chấm!", HttpStatus.BAD_REQUEST);
        }

        submission.setScore(score);
        submission.setTeacherComment(comment);
        submission.setGradedAt(LocalDateTime.now());

        Submission updated = submissionJpaRepository.save(submission);
        return convertToDto(updated);
    }

    public List<SubmissionResponseDto> getSubmissionsByAssignment(Integer assignmentId) {
        List<Submission> submissions = submissionJpaRepository.findByAssignment_Id(assignmentId);
        return submissions.stream().map(this::convertToDto).toList();
    }

    public List<SubmissionResponseDto> getSubmissionsByStudent(Integer studentId) {
        return submissionJpaRepository.findByStudentId(studentId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public SubmissionResponseDto getSubmission(Integer assignmentId, Integer studentId) {
        Submission submission = submissionJpaRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new EntityNotFoundException("Submission"));
        return convertToDto(submission);
    }

    // Get submission by class
    public List<SubmissionResponseDto> getSubmissionsByClassId(Integer classId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class with id: " + classId));

        return submissionJpaRepository.findByClassId(classId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // Get submission by student, class
    public List<SubmissionResponseDto> getSubmissionsByClassIdAndStudentId(Integer classId, Integer studentId) {
        classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + classId));

        return submissionJpaRepository.findByClassIdAndStudentId(classId, studentId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // Tải tệp đính kèm bài nộp về máy
    public DownloadFileDTO downloadSubmission(Integer id) throws Exception {
        // 1. Lấy thông tin bài nộp
        Submission submission = submissionJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Submission with id: " + id));

        String fileUrl = submission.getFilePath();
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new EntityNotFoundException("File not found for submission " + id);
        }

        // 2. Lấy file từ Cloudinary URL
        UrlResource resource = new UrlResource(new URL(fileUrl));

        if (!resource.exists()) {
            throw new EntityNotFoundException("File not found at " + fileUrl);
        }

        // 3. Trả DTO chứa file và metadata
        return new DownloadFileDTO(
                resource,
                submission.getFileType() != null ? submission.getFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE,
                submission.getFileName() != null ? submission.getFileName() : "file");
    }

    @LoggableAction(value = "DELETE", entity = "Submission", description = "Deleted a submission")
    public void deleteSubmission(Integer submissionId) {
        Submission submission = submissionJpaRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission"));

        // ✅ Xóa file Cloudinary
        if (submission.getFilePath() != null && !submission.getFilePath().isBlank()) {
            try {
                String publicId = CloudinaryUtils.extractPublicId(submission.getFilePath());
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (Exception e) {
                log.warn("Không thể xóa file trên Cloudinary: {}", submission.getFilePath(), e);
            }
        }

        // Xóa dữ liệu trong DB
        submissionJpaRepository.delete(submission);
    }
}
