package com.example.springboot_education.services.assignment;

import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.dtos.assignmentDTOs.*;
import com.example.springboot_education.dtos.materialDTOs.DownloadFileDTO;
import com.example.springboot_education.entities.Assignment;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.assignment.AssignmentJpaRepository;
import com.example.springboot_education.services.SlackService;
import com.example.springboot_education.untils.FileUtils;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AssignmentService {
    private final NotificationServiceAssignment notificationService;
    private final SlackService slackService;

    private final AssignmentJpaRepository assignmentJpaRepository;
    private final ClassRepository classRepository;
    // Xóa ActivityLogService khỏi đây
    // private final ActivityLogService activityLogService;

    private AssignmentResponseDto convertToDto(Assignment assignment) {
        AssignmentResponseDto dto = new AssignmentResponseDto();
        dto.setId(assignment.getId());
        dto.setClassId(assignment.getClassField().getId());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setDueDate(assignment.getDueDate());
        dto.setMaxScore(assignment.getMaxScore());
        dto.setFilePath(assignment.getFilePath());
        dto.setFileType(assignment.getFileType());
        dto.setFileSize(FileUtils.formatFileSize(assignment.getFileSize()));
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setUpdatedAt(assignment.getUpdatedAt());
        return dto;
    }

    public List<AssignmentResponseDto> getAllAssignments() {
        return assignmentJpaRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public AssignmentResponseDto getAssignmentById(Integer id) {
        Assignment assignment = assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment with id: " + id));
        return convertToDto(assignment);
    }

    @LoggableAction(value = "CREATE", entity = "assignments", description = "Created a new assignment")
    public AssignmentResponseDto createAssignmentWithFile(CreateAssignmentRequestDto dto, MultipartFile file)
            throws IOException {
        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class with id: " + dto.getClassId()));

        String uploadDir = "uploads/assignments";
        Files.createDirectories(Paths.get(uploadDir));

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        Files.write(filePath, file.getBytes());

        Assignment assignment = new Assignment();
        assignment.setClassField(classEntity);
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setMaxScore(dto.getMaxScore());
        assignment.setFilePath(filePath.toString());
        assignment.setFileType(file.getContentType());
        assignment.setFileSize(file.getSize());

        Assignment saved = assignmentJpaRepository.save(assignment);
        Map<String,Object> payload = Map.of(
                "teacher", saved.getClassField().getTeacher().getFullName(),
                "title",   saved.getTitle()
        );
        slackService.sendSlackNotification(
                saved.getClassField().getId(),
                SlackService.ClassEventType.ASSIGNMENT_CREATED,
                payload
        );
        NotificationAssignmentDTO notifyPayload = NotificationAssignmentDTO.builder()
            .classId(dto.getClassId())
            .title(saved.getTitle())
            .description(saved.getDescription())
            .dueDate(saved.getDueDate())
            .build();

        notificationService.notifyClass(dto.getClassId(), notifyPayload);
        // Xóa code ghi log thủ công
        // activityLogService.log(...);

        return convertToDto(saved);
    }

    // Update
    @LoggableAction(value = "UPDATE", entity = "assignments", description = "Updated an assignment")
    public AssignmentResponseDto updateAssignment(Integer id, UpdateAssignmentRequestDto dto, MultipartFile file) throws IOException {
        Assignment assignment = assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment with id: " + id));

        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class with id: " + dto.getClassId()));

        assignment.setTitle(dto.getTitle());
        assignment.setClassField(classEntity);
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setMaxScore(dto.getMaxScore());

        // Nếu có file mới thì thay thế
        if (file != null && !file.isEmpty()) {
            String uploadDir = "uploads/assignments";
            Files.createDirectories(Paths.get(uploadDir));

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, file.getBytes());

            assignment.setFilePath(filePath.toString());
            assignment.setFileType(file.getContentType());
            assignment.setFileSize(file.getSize());
        }

        Assignment updated = assignmentJpaRepository.save(assignment);
        return convertToDto(updated);
    }

    @LoggableAction(value = "DELETE", entity = "assignments", description = "Deleted an assignment")
    public void deleteAssignment(Integer id) {
        Assignment assignment = assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment with id: " + id));

        // Xóa code ghi log thủ công
        // activityLogService.log(...);

        assignmentJpaRepository.delete(assignment);
    }

    public Assignment getAssignmentEntityById(Integer id) {
        return assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment with id: " + id));
    }

    // Get assignment by class
    public List<AssignmentResponseDto> getAssignmentsByClassId(Integer classId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class with id: " + classId));

        return assignmentJpaRepository.findByClassField_Id(classId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // Tải tệp đính kèm bài tập về máy
    public DownloadFileDTO downloadAssignment(Integer id) throws Exception {
        // 1. Lấy thông tin bài tập
        Assignment assignment = assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment with id: " + id));

        // 2. Lấy file từ đường dẫn (dùng path tuyệt đối)
        Path path = Paths.get(assignment.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            throw new EntityNotFoundException("File");
        }

        // 3. Trả DTO chứa file và metadata
        return new DownloadFileDTO(
                resource,
                assignment.getFileType() != null ? assignment.getFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE,
                path.getFileName().toString()
        );
    }
public List<UpcomingAssignmentDto> getUpcomingAssignments(Integer studentId) {
    List<Assignment> assignments = assignmentJpaRepository.findAssignmentsByStudentId(studentId);

    return assignments.stream().map(a -> {
        try {
            int daysLeft = -1;
//            if (a.getDueDate() != null) {
//                Date utilDate = new Date(a.getDueDate().getTime());
//                LocalDate due = utilDate.toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDate();
//                daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), due);
//            }
            if (a.getDueDate() != null) {
                // do assignment.getDueDate() là LocalDateTime nên chỉ cần toLocalDate()
                LocalDate due = a.getDueDate().toLocalDate();
                daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), due);
            }

            return UpcomingAssignmentDto.builder()
                    .id(a.getId())
                    .title(a.getTitle())
                    .className(a.getClassField() != null ? a.getClassField().getClassName() : "Unknown")
                    .dueDate(a.getDueDate())
                    .daysLeft(daysLeft)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }).collect(Collectors.toList());
}


public List<UpcomingSubmissionDto> getUpcomingSubmissions(Integer teacherId) {
        List<Assignment> assignments = assignmentJpaRepository.findAssignmentsByTeacherId(teacherId);

        return assignments.stream().map(a -> {
            try {
                int daysLeft = -1;
//                if (a.getDueDate() != null) {
//                    Date utilDate = new Date(a.getDueDate().getTime());
//                    LocalDate due = utilDate.toInstant()
//                            .atZone(ZoneId.systemDefault())
//                            .toLocalDate();
//                    daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), due);
//                }
                if (a.getDueDate() != null) {
                    LocalDate due = a.getDueDate().toLocalDate();
                    daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), due);
                }

                // Assume getSubmissions() is now correctly defined on the Assignment entity.
                int submittedCount = a.getSubmissions() != null ? a.getSubmissions().size() : 0;

                // CORRECTED: Access classUsers list to get total student count.
                int totalStudents = (a.getClassField() != null && a.getClassField().getClassUsers() != null) 
                                     ? a.getClassField().getClassUsers().size() : 0;

                return UpcomingSubmissionDto.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .className(a.getClassField() != null ? a.getClassField().getClassName() : "Unknown")
                        .dueDate(a.getDueDate())
                        .daysLeft(daysLeft)
                        .submittedCount(submittedCount)
                        .totalStudents(totalStudents)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }).collect(Collectors.toList());
    }

}