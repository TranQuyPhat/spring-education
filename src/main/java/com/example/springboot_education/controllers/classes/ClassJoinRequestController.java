package com.example.springboot_education.controllers.classes;



import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.springboot_education.dtos.joinrequest.CreateJoinRequestRequest;
import com.example.springboot_education.dtos.joinrequest.JoinRequestDTO;
import com.example.springboot_education.dtos.joinrequest.JoinRequestResponseDTO;
import com.example.springboot_education.entities.ClassJoinRequest;
import com.example.springboot_education.services.classes.ClassJoinRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassJoinRequestController {

    private final ClassJoinRequestService service;

    @PostMapping("/{classId}/join-request")
    public ResponseEntity<JoinRequestDTO> createJoinRequest(
            @PathVariable("classId") Integer classId,
            @RequestBody CreateJoinRequestRequest body) {

        return ResponseEntity.ok(service.joinClass(classId, body.getStudentId()));
    }

    @GetMapping("/{classId}/join-requests")
    public ResponseEntity<List<JoinRequestDTO>> getRequests(
            @PathVariable("classId") Integer classId,
            @RequestParam(value = "status", required = false) String status) {

        ClassJoinRequest.Status s = status != null
                ? ClassJoinRequest.Status.valueOf(status.toUpperCase())
                : null;

        return ResponseEntity.ok(service.getRequestsForClass(classId, s));
    }
    @GetMapping("/join-requests")
    public ResponseEntity<List<JoinRequestDTO>> getRequestsForTeacher(
            @RequestParam("teacherId") Integer teacherId,
            @RequestParam(value = "status", required = false) String status) {

        ClassJoinRequest.Status s = status != null
                ? ClassJoinRequest.Status.valueOf(status.toUpperCase())
                : null;

        return ResponseEntity.ok(service.getRequestsForTeacher(teacherId, s));
    }

    @PostMapping("/join-requests/{requestId}/approve")
    public ResponseEntity<JoinRequestResponseDTO> approve(@PathVariable("requestId") Integer requestId) {
        return ResponseEntity.ok(service.approve(requestId, 2)); // TODO: lấy teacherId từ Auth
    }

    @PostMapping("/join-requests/{requestId}/reject")
    public ResponseEntity<JoinRequestResponseDTO> reject(
            @PathVariable("requestId") Integer requestId,
            @RequestBody(required = false) String reason) {
        return ResponseEntity.ok(service.reject(requestId, 2, reason));
    }
}
