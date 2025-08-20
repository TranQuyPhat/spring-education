package com.example.springboot_education.dtos.gradeDTOs;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GradeResultDTO {
    private String type;
    private String title;
    private BigDecimal score;
    private BigDecimal maxScore;
    private Instant submittedAt;
    private Instant gradedAt;
    private String status;
}
