package com.example.springboot_education.services.assignment;

import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.assignmentDTOs.AssignmentResponseDto;
import com.example.springboot_education.dtos.materialDTOs.DownloadFileDTO;
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
import com.example.springboot_education.untils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionJpaRepository submissionJpaRepository;
    private final AssignmentJpaRepository assignmentJpaRepository;
    private final UsersJpaRepository usersJpaRepository;
    private final ClassRepository classRepository;

    private SubmissionResponseDto convertToDto(Submission submission) {
        SubmissionResponseDto dto = new SubmissionResponseDto();
        dto.setId(submission.getId());
        dto.setAssignmentId(submission.getAssignment().getId());
        dto.setFilePath(submission.getFilePath());
        dto.setFileType(submission.getFileType());
        dto.setFileSize(FileUtils.formatFileSize(submission.getFileSize()));
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

        // Kiểm tra đã nộp chưa
        submissionJpaRepository.findByAssignmentIdAndStudentId(
                requestDto.getAssignmentId(), requestDto.getStudentId()).ifPresent(existing -> {
                    throw new HttpException("You have already submitted this assignment", HttpStatus.CONFLICT);
                });

        String uploadDir = "uploads/submissions";
        Files.createDirectories(Paths.get(uploadDir));

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        Files.write(filePath, file.getBytes());

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFilePath(filePath.toString());
        submission.setFileType(file.getContentType());
        submission.setFileSize(file.getSize());
        submission.setDescription(requestDto.getDescription());
        submission.setStatus(Submission.SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(now);

        Submission saved = submissionJpaRepository.save(submission);
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

        // Nếu có file mới thì thay thế
        if (newFile != null && !newFile.isEmpty()) {
            // Xóa file cũ
            try {
                Path oldPath = Paths.get(submission.getFilePath());
                Files.deleteIfExists(oldPath);
            } catch (IOException e) {
                throw new HttpException("Không thể xóa file cũ", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Lưu file mới
            String uploadDir = "uploads/submissions";
            Files.createDirectories(Paths.get(uploadDir));

            String filename = UUID.randomUUID() + "_" + newFile.getOriginalFilename();
            Path newPath = Paths.get(uploadDir, filename);
            Files.write(newPath, newFile.getBytes());

            submission.setFilePath(newPath.toString());
            submission.setFileType(newFile.getContentType());
            submission.setFileSize(newFile.getSize());
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

    public String storeFile(MultipartFile file) throws IOException {
        // Validate loại file
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("application/pdf") ||
                        contentType.equals("application/msword") ||
                        contentType
                                .equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new HttpException("File type is not supported. Allowed: PDF, DOC, DOCX", HttpStatus.BAD_REQUEST);
        }

        String uploadDir = System.getProperty("user.dir") + "/uploads/assignments";
        Files.createDirectories(Paths.get(uploadDir)); // tạo thư mục nếu chưa có

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        Files.write(filePath, file.getBytes());

        return "/uploads/assignments/" + filename; // lưu đường dẫn vào DB
    }

    // Tải tệp đính kèm bài nộp về máy
    public DownloadFileDTO downloadSubmission(Integer id) throws Exception {
        // 1. Lấy thông tin bài nộp
        Submission submission = submissionJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Submission with id: " + id));

        // 2. Lấy file từ đường dẫn (dùng path tuyệt đối)
        Path path = Paths.get(submission.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            throw new EntityNotFoundException("File");
        }

        // 3. Trả DTO chứa file và metadata
        return new DownloadFileDTO(
                resource,
                submission.getFileType() != null ? submission.getFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE,
                path.getFileName().toString());
    }

    @LoggableAction(value = "DELETE", entity = "Submission", description = "Deleted a submission")
    public void deleteSubmission(Integer submissionId) {
        Submission submission = submissionJpaRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission"));

        // Xóa file vật lý nếu tồn tại
        try {
            Path filePath = Paths.get(submission.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new HttpException("Failed to delete file", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Xóa dữ liệu trong DB
        submissionJpaRepository.delete(submission);
    }
}
