package com.example.springboot_education.dtos.activitylogs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogCreateDTO {
    private String actionType;
    private Integer targetId;
    private String targetTable;
    private String description;
    private Integer classId;
    private Integer userId;
}
