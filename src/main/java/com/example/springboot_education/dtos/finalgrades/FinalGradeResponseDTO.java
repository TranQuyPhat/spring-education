package com.example.springboot_education.dtos.finalgrades;

import lombok.Data;

@Data
public class FinalGradeResponseDTO {
    private Long id;
    private Long studentId;
    private String studentName;  // Optional: nếu JOIN bảng Users
    private Long classId;
    private String className;    // Optional: nếu JOIN bảng Classes
    private String semester;
    private Integer schoolYear;
    private Double averageScore;
    private String letterGrade;
}
