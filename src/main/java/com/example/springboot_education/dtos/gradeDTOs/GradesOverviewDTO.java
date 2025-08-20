package com.example.springboot_education.dtos.gradeDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradesOverviewDTO {
    private BigDecimal avgScore;
    private long gradedCount;
    private long lateCount;
    private long missingCount;
    private List<ScoreRangeDTO> scoreDistribution;
}
