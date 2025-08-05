package com.example.springboot_education.dtos.activitylogs;

import java.time.Instant;

import lombok.Data;

@Data
public class ActivityLogResponseDTO {
    private Integer id;
    private Integer userId;
    private String actionType;
    private String targetTable;
    private Integer targetId;
    private String description;
    private Instant createdAt;
}

