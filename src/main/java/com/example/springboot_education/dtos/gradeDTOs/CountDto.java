package com.example.springboot_education.dtos.gradeDTOs;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountDto {
    private long total;
    private long graded;
    private long pending;
}