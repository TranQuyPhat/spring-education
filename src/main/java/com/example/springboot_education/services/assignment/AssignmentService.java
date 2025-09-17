package com.example.springboot_education.services.assignment;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.untils.CloudinaryUtils;
import com.example.springboot_education.untils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.springboot_education.annotations.LoggableAction; // Import annotation
import com.example.springboot_education.dtos.assignmentDTOs.AssignmentResponseDto;
import com.example.springboot_education.dtos.assignmentDTOs.CreateAssignmentRequestDto;
import com.example.springboot_education.dtos.assignmentDTOs.NotificationAssignmentDTO;
import com.example.springboot_education.dtos.assignmentDTOs.UpcomingAssignmentDto;
import com.example.springboot_education.dtos.assignmentDTOs.UpcomingSubmissionDto;
import com.example.springboot_education.dtos.assignmentDTOs.UpdateAssignmentRequestDto;
import com.example.springboot_education.entities.Assignment;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.assignment.AssignmentJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Slf4j
public class AssignmentService {
    private final Cloudinary cloudinary;
    private final NotificationServiceAssignment notificationService;

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
        dto.setFileName(assignment.getFileName());
        dto.setPublished(assignment.isPublished());
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

        // 🔹 Upload file lên Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "assignments",
                        "resource_type", "auto"
                ));

        String fileUrl = (String) uploadResult.get("secure_url");
        String originalFilename = file.getOriginalFilename();

        Assignment assignment = new Assignment();
        assignment.setClassField(classEntity);
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setMaxScore(dto.getMaxScore());
        assignment.setFilePath(fileUrl); // ✅ Lưu URL Cloudinary
        assignment.setFileType(file.getContentType());
        assignment.setFileSize(file.getSize());
        assignment.setFileName(originalFilename);

        Assignment saved = assignmentJpaRepository.save(assignment);


        NotificationAssignmentDTO notifyPayload = NotificationAssignmentDTO.builder()
                .classId(dto.getClassId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .dueDate(saved.getDueDate())
                .build();

        notificationService.notifyClass(dto.getClassId(), notifyPayload);

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

        // Nếu có file mới thì xử lý upload
        if (file != null && !file.isEmpty()) {
            // Xóa file cũ trên Cloudinary (nếu có)
            if (assignment.getFilePath() != null && !assignment.getFilePath().isBlank()) {
                try {
                    String publicId = CloudinaryUtils.extractPublicId(assignment.getFilePath());
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                } catch (Exception e) {
                    log.warn("Không thể extract/xóa file cũ trên Cloudinary: {}", assignment.getFilePath(), e);
                }
            }

            try {
                Map<String, Object> uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap("resource_type", "auto")
                );

                assignment.setFilePath(uploadResult.get("secure_url").toString());
                assignment.setFileType(file.getContentType());
                assignment.setFileSize(file.getSize());
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload file to Cloudinary: " + e.getMessage(), e);
            }
        }

        Assignment updated = assignmentJpaRepository.save(assignment);
        return convertToDto(updated);
    }

    @LoggableAction(value = "DELETE", entity = "assignments", description = "Deleted an assignment")
    public void deleteAssignment(Integer id) {
        Assignment assignment = assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment with id: " + id));
        // 🔹 Xoá file trên Cloudinary (nếu có)
        if (assignment.getFilePath() != null) {
            try {
                String publicId = CloudinaryUtils.extractPublicId(assignment.getFilePath());
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete file from Cloudinary: " + e.getMessage());
            }
        }
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
    public String downloadAssignment(Integer id) {
        Assignment assignment = assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment with id: " + id));

        if (assignment.getFilePath() == null) {
            throw new EntityNotFoundException("File not found for assignment id: " + id);
        }

        return assignment.getFilePath() + "?fl_attachment";
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

    // công bố điểm
    @LoggableAction(value = "UPDATE", entity = "assignments", description = "Published assignment results")
    public AssignmentResponseDto publishAssignment(Integer id) {
        Assignment assignment = assignmentJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment with id: " + id));

        assignment.setPublished(true);  // set isPublished = true
        Assignment updated = assignmentJpaRepository.save(assignment);

        return convertToDto(updated);
    }


}