package com.example.springboot_education.dtos.usersDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDto {
    private String fullName;
    private String avatarBase64;
}
