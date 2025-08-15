package com.example.springboot_education.dtos.AuthDTOs;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleLoginRequestDto {
    private String name;
    private String email;
    private String imageUrl;
}

