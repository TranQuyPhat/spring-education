package com.example.springboot_education.dtos.gradeDTOs;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompareDto {
    private BigDecimal studentAvg;
    private BigDecimal classAvg;
    private BigDecimal difference;
}