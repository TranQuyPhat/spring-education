package com.example.springboot_education.dtos.dashboard.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectGradeDTO {
    private Integer id;
    private String subject;
    private String className;
    private List<AssignmentDTO> assignments;
    private BigDecimal average;
    private String trend;
}