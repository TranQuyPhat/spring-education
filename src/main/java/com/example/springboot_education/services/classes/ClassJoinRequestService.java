package com.example.springboot_education.services.classes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springboot_education.dtos.joinrequest.ApprovalResponseDTO;
import com.example.springboot_education.dtos.joinrequest.JoinRequestDTO;
import com.example.springboot_education.dtos.joinrequest.JoinRequestResponseDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.Users;

import com.example.springboot_education.entities.ClassJoinRequest;
import com.example.springboot_education.repositories.ClassJoinRequestRepository;
import com.example.springboot_education.services.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassJoinRequestService {

    private final ClassJoinRequestRepository repo;
    private final NotificationService notificationService;
    private final ClassService classService;
    private final ClassUserService classUserService;
    private final UserService userService;

        public JoinRequestDTO createRequest(Integer classId, Integer studentId) {
        if (repo.existsByClassEntity_IdAndStudent_IdAndStatus(
                classId, studentId, ClassJoinRequest.Status.PENDING
        )) {
                throw new IllegalArgumentException("Yêu cầu đã tồn tại và đang chờ duyệt.");
        }

        ClassEntity classEntity = classService.getClassEntityById(classId);
        Users student = userService.getUserEntityById(studentId);

        ClassJoinRequest saved = repo.save(
                ClassJoinRequest.builder()
                        .classEntity(classEntity)
                        .student(student)
                        .status(ClassJoinRequest.Status.PENDING)
                        .build()
        );

        Integer teacherId = classService.getTeacherIdOfClass(classId);

        JoinRequestDTO dto = convertToDTO(saved); // dùng method convert chung
        notificationService.notifyTeacher(teacherId, dto);

        return dto;
        }

    public List<JoinRequestDTO> getRequestsForClass(Integer classId, ClassJoinRequest.Status status) {
        List<ClassJoinRequest> requests = status != null
                ? repo.findByClassEntity_IdAndStatus(classId, status)
                : repo.findByClassEntity_Id(classId);

        return convertListToDTOs(requests);
        }
        public List<JoinRequestDTO> getRequestsForTeacher(Integer teacherId, ClassJoinRequest.Status status) {
        List<ClassJoinRequest> requests = status != null
                ? repo.findByClassEntity_Teacher_IdAndStatus(teacherId, status)
                : repo.findByClassEntity_Teacher_Id(teacherId);

        return convertListToDTOs(requests);
        }

@Transactional
public JoinRequestResponseDTO approve(Integer requestId, Integer teacherId) {
    ClassJoinRequest req = repo.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));

    req.setStatus(ClassJoinRequest.Status.APPROVED);
    classUserService.addStudentToClass(req.getClassEntity().getId(), req.getStudent().getId());

    notificationService.notifyStudent(req.getStudent().getId(),
            ApprovalResponseDTO.builder()
                    .requestId(req.getId())
                    .approved(true)
                    .message("Yêu cầu của bạn đã được duyệt.")
                    .build());

    return JoinRequestResponseDTO.builder()
            .requestId(req.getId())
            .classId(req.getClassEntity().getId())
            .studentId(req.getStudent().getId())
            .studentName(req.getStudent().getFullName())
            .status(req.getStatus())
            .message("Yêu cầu đã được duyệt thành công")
            .build();
}

@Transactional
public JoinRequestResponseDTO reject(Integer requestId, Integer teacherId, String reason) {
    ClassJoinRequest req = repo.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));

    req.setStatus(ClassJoinRequest.Status.REJECTED);

    notificationService.notifyStudent(req.getStudent().getId(),
            ApprovalResponseDTO.builder()
                    .requestId(req.getId())
                    .approved(false)
                    .message(reason != null ? reason : "Yêu cầu của bạn đã bị từ chối.")
                    .build());

    return JoinRequestResponseDTO.builder()
            .requestId(req.getId())
            .classId(req.getClassEntity().getId())
            .studentId(req.getStudent().getId())
            .studentName(req.getStudent().getFullName())
            .status(req.getStatus())
            .message(reason != null ? reason : "Yêu cầu đã bị từ chối")
            .build();
}

        private JoinRequestDTO convertToDTO(ClassJoinRequest r) {
        return JoinRequestDTO.builder()
                .requestId(r.getId())
                .classId(r.getClassEntity().getId())
                .className(r.getClassEntity().getClassName())
                .studentId(r.getStudent().getId())
                .studentName(r.getStudent().getFullName())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
        }

        private List<JoinRequestDTO> convertListToDTOs(List<ClassJoinRequest> requests) {
        return requests.stream()
                .map(this::convertToDTO)
                .toList();
        }
}
