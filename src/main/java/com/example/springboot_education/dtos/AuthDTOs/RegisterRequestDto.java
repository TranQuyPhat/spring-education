package com.example.springboot_education.dtos.AuthDTOs;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class RegisterRequestDto {
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @AssertTrue(message = "Either username or email is required")
    public boolean isUsernameOrEmailProvided() {
        return (username != null && !username.isBlank()) ||
               (email != null && !email.isBlank());
    }

    private String fullName;
    private String roleName;
}

