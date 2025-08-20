package com.example.springboot_education.dtos.joinrequest;

import com.example.springboot_education.entities.ClassJoinRequest.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinRequestResponseDTO {
    private Integer requestId;
    private Integer classId;
    private Integer studentId;
    private String studentName;
    private Status status;
    private String message;
}
