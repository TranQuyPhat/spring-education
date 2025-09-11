package com.example.springboot_education.controllers;


import com.example.springboot_education.dtos.PasswordDtos;
import com.example.springboot_education.services.mail.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/password")
public class PasswordController {

    private final PasswordService passwordService;
    @PostMapping("/change/request-otp")
    public ResponseEntity<?> requestChangePasswordOtp(@RequestBody @Valid PasswordDtos.ChangePasswordOtpRequest req) {
        passwordService.sendChangePasswordOtp(req.getEmail());
        return ResponseEntity.ok().body("{\"message\":\"OTP sent\"}");
    }

    @PostMapping("/change")
    public ResponseEntity<?> changePassword(@RequestBody @Valid PasswordDtos.ChangePasswordRequest req) {
        passwordService.changePasswordWithOtp(
                req.getEmail(),
                req.getOtp(),
                req.getOldPassword(),
                req.getNewPassword()
        );
        return ResponseEntity.ok().body("{\"message\":\"Password changed\"}");
    }

    @GetMapping("/change/cooldown")
    public ResponseEntity<?> getChangeCooldown(@RequestParam String email) {
        long seconds = passwordService.getChangePasswordCooldown(email);
        return ResponseEntity.ok().body("{\"cooldown_remaining_seconds\":" + seconds + "}");
    }

    // --- FORGOT ---
    @PostMapping("/forgot/request-otp")
    public ResponseEntity<?> requestForgotPasswordOtp(@RequestBody @Valid PasswordDtos.ForgotPasswordOtpRequest req) {
        passwordService.sendForgotPasswordOtp(req.getEmail());
        return ResponseEntity.ok().body("{\"message\":\"OTP sent\"}");
    }

    @PostMapping("/forgot/verify-otp")
    public ResponseEntity<?> verifyForgotOtp(@RequestBody @Valid PasswordDtos.VerifyForgotOtpRequest req) {
        String resetToken = passwordService.verifyForgotPasswordOtpIssueToken(req.getEmail(), req.getOtp());
        return ResponseEntity.ok(new PasswordDtos.VerifyForgotOtpResponse(resetToken));
    }

    @PostMapping("/forgot/reset")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordDtos.ResetPasswordRequest req) {
        passwordService.resetPasswordWithToken(req.getResetToken(), req.getNewPassword(), req.getConfirmPassword());
        return ResponseEntity.ok().body("{\"message\":\"Password reset\"}");
    }

    @GetMapping("/forgot/cooldown")
    public ResponseEntity<?> getForgotCooldown(@RequestParam String email) {
        long seconds = passwordService.getForgotPasswordCooldown(email);
        return ResponseEntity.ok().body("{\"cooldown_remaining_seconds\":" + seconds + "}");
    }
}