package com.example.springboot_education.dtos.AuthDTOs;

import lombok.Data;
@Data
public class RegisterRequestDto {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String roleName; 
}

