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


    // Constructor 5 tham số
    public ActivityLogCreateDTO(String actionType, Integer targetId, String targetTable, String description, Integer userId) {
        this.actionType = actionType;
        this.targetId = targetId;
        this.targetTable = targetTable;
        this.description = description;
        this.userId = userId;
        this.classId = null; // Mặc định null khi không truyền
    }

  

}
