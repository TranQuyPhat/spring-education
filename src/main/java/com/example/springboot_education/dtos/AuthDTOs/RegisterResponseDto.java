package com.example.springboot_education.dtos.AuthDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponseDto {
    private Integer userId;
    private String username;
    private String email;
    private String accessToken;
    private String workspaceInviteLink;
}