package com.example.springboot_education.dtos.AuthDTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    private String username;
    private String email;
    private String password;
}