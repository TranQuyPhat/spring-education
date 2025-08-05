package com.example.springboot_education.dtos.assignmentDTOs;

import lombok.*;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequestDto {

    private Integer classId;
    private String title;
    private String description;

    @NotNull(message = "Class ID is required")
    private Integer classId;

    @NotNull(message = "Due date is required")
    private Timestamp dueDate;

    @NotNull(message = "Max score is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Max score must be positive")
    private BigDecimal maxScore;
    private String filePath;
    private String fileType;
}
