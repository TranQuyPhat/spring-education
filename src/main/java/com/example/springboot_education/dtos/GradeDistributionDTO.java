package com.example.springboot_education.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GradeDistributionDTO {
    private long xuatSac;        // số học sinh ≥ 9.0
    private long gioi;           // 8.0 – 8.9
    private long kha;            // 6.5 – 7.9
    private long canCaiThien;    // < 6.5
    private long totalStudents;  // tổng số học sinh
}
