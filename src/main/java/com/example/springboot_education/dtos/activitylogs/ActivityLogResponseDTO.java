package com.example.springboot_education.dtos.activitylogs;

import java.time.Instant;

import lombok.Data;

@Data
public class ActivityLogResponseDTO {
    private Integer id;
    private String actionType;
    private Integer targetId;
    private String targetTable;
    private String description;
    private Instant createdAt;
    private Integer classId;
    private Integer userId;
}

