package com.example.springboot_education.dtos.AuthDTOs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private Integer userId;
    private String username;
    private String email;
    private String accessToken;
     private List<String> roles; 
         private boolean requireRoleSelection;
    private String fullName;
    private String avatarBase64;
}