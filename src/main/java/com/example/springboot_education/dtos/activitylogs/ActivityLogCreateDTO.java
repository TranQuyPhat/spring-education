package com.example.springboot_education.dtos.activitylogs;

<<<<<<< HEAD
=======
import lombok.AllArgsConstructor;
>>>>>>> 9ced5b3 (feat: hoàn thành tính năng activity_logs)
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
<<<<<<< HEAD
=======
@AllArgsConstructor
>>>>>>> 9ced5b3 (feat: hoàn thành tính năng activity_logs)
public class ActivityLogCreateDTO {
    private String actionType;
    private Integer targetId;
    private String targetTable;
    private String description;
    private Integer classId;
    private Integer userId;
<<<<<<< HEAD

    // Constructor 5 tham số
    public ActivityLogCreateDTO(String actionType, Integer targetId, String targetTable, String description, Integer userId) {
        this.actionType = actionType;
        this.targetId = targetId;
        this.targetTable = targetTable;
        this.description = description;
        this.userId = userId;
        this.classId = null; // Mặc định null khi không truyền
    }

    // Constructor 6 tham số
    public ActivityLogCreateDTO(String actionType, Integer targetId, String targetTable, String description, Integer classId, Integer userId) {
        this.actionType = actionType;
        this.targetId = targetId;
        this.targetTable = targetTable;
        this.description = description;
        this.classId = classId;
        this.userId = userId;
    }
=======
>>>>>>> 9ced5b3 (feat: hoàn thành tính năng activity_logs)
}
