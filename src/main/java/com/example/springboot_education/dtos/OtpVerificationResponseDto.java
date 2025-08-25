package com.example.springboot_education.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// OtpVerificationResponseDto.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationResponseDto {
    private Boolean isValid;
    private String message;
    private Long remainingAttempts;
}
