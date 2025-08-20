package com.example.springboot_education.dtos.AuthDTOs;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginWithCredentialRequestDto {
    @NotEmpty
    private String credential;
}