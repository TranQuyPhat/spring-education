package com.example.springboot_education.dtos.joinrequest;


import lombok.*;

import java.time.Instant;

import com.example.springboot_education.entities.ClassJoinRequest.Status;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequestDTO {
    private Integer requestId;
    @NotNull(message = "Student ID is required")
    private Integer studentId;

    @NotNull(message = "Class ID is required")
    private Integer classId;
    private String studentName;
    private String className;
    private Status status;
    private String message;
    private Instant createdAt;
}