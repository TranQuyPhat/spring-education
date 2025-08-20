package com.example.springboot_education.dtos.dashboard.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDTO {
    private String name;
    private BigDecimal grade;
    private BigDecimal maxGrade;
    private String type;
    private LocalDateTime date;
}