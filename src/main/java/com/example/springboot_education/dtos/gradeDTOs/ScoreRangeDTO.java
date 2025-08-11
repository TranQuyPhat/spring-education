package com.example.springboot_education.dtos.gradeDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreRangeDTO {
    private String range;
    private long count;
}
