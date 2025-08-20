package com.example.springboot_education.dtos.activitylogs;

import java.time.Instant;

import com.example.springboot_education.entities.ActivityLog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponseDTO {
    private Integer id;
    private Integer userId; 
    private String fullName; 
    private String actionType;
    private String targetTable;
    private Integer targetId;
    private String description;
    private Instant createdAt;
    private Integer classId;

    public ActivityLogResponseDTO(ActivityLog activityLog) {
        this.id = activityLog.getId(); 
        this.userId = activityLog.getUser() != null ? activityLog.getUser().getId().intValue() : null; 
        this.fullName = activityLog.getUser() != null ? activityLog.getUser().getFullName() : "N/A"; 

        this.actionType = activityLog.getActionType();
        this.targetTable = activityLog.getTargetTable();
        this.targetId = activityLog.getTargetId();
        this.description = activityLog.getDescription();
        this.createdAt = activityLog.getCreatedAt();

        this.classId = null; 
    }
}
