package com.example.springboot_education.dtos.gradeDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentDetailDTO {
    private Integer assignmentId;
    private String title;
    private BigDecimal score;
    private String status;
    private String dueDate;
}
