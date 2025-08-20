package com.example.springboot_education.dtos.joinrequest;


import lombok.*;

import java.time.Instant;

import com.example.springboot_education.entities.ClassJoinRequest.Status;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequestDTO {
    private Integer requestId;
    private Integer classId;
    private Integer studentId;
    private String studentName;
    private String className;
    private Status status;
    private Instant createdAt;
}