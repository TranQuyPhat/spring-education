package com.example.springboot_education.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class PasswordDtos {

    @Data
    public static class ChangePasswordOtpRequest {
        @Email @NotBlank
        private String email;
    }

    @Data
    public static class ChangePasswordRequest {
        @Email @NotBlank
        private String email;
        @NotBlank
        private String otp;
        @NotBlank
        private String oldPassword;
        @NotBlank
        private String newPassword;
    }

    @Data
    public static class ForgotPasswordOtpRequest {
        @Email @NotBlank
        private String email;
    }

    @Data
    public static class VerifyForgotOtpRequest {
        @Email @NotBlank
        private String email;
        @NotBlank
        private String otp;
    }

    @Data
    public static class VerifyForgotOtpResponse {
        private String resetToken;
        public VerifyForgotOtpResponse(String resetToken) { this.resetToken = resetToken; }
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank
        private String resetToken;
        @NotBlank
        private String newPassword;
        @NotBlank
        private String confirmPassword;
    }
}