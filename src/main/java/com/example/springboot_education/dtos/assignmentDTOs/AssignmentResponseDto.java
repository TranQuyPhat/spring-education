package com.example.springboot_education.dtos.assignmentDTOs;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;


@Getter
@Setter
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AssignmentResponseDto {
    private Integer id;
    private String title;
    private String description;
    private Integer classId;
    private Date dueDate;
    private BigDecimal maxScore;
    private String filePath;
    private String fileType;
    private Timestamp createdAt;
    private Timestamp updatedAt;

}
