package com.example.springboot_education.services.mail;

import com.example.springboot_education.entities.OtpVerification;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.repositories.PendingUserRepository;
import com.example.springboot_education.repositories.otp.OtpVerificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final PendingUserRepository pendingUserRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_LENGTH = 6;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_MINUTES = 2;
    private static final int RESEND_COOLDOWN_SECONDS = 60; // ✔ 60s
    private static final int OTP_EXPIRE_MINUTES = 5;
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    public void sendOtp(String email, String fullName) {
        // Kiểm tra rate limit - không cho gửi liên tục
        checkResendCooldown(email);

        // Xóa các OTP cũ của email này
        cleanupOldOtps(email);

        // Tạo OTP mới
        String otp = generateOtp();

        OtpVerification otpVerification = OtpVerification.builder()
                .email(email)
                .otp(otp)
                .build();

        otpRepository.save(otpVerification);

        // Gửi email
        emailService.sendOtpEmail(email, otp, fullName);

        log.info("OTP sent to email: {}", email);
    }

    public boolean verifyOtp(String email, String otp) {
        // Tìm OTP hợp lệ
        Optional<OtpVerification> otpVerificationOpt =
                otpRepository.findByEmailAndOtpAndIsUsedFalse(email, otp);

        if (otpVerificationOpt.isEmpty()) {
            log.warn("Invalid OTP attempt for email: {}", email);
            incrementAttemptCount(email);
            return false;
        }

        OtpVerification otpVerification = otpVerificationOpt.get();

        // Kiểm tra hết hạn
        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Expired OTP attempt for email: {}", email);
            return false;
        }

        // Kiểm tra số lần thử
        if (otpVerification.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
            log.warn("Too many OTP attempts for email: {}", email);
            throw new HttpException("Too many failed attempts. Please request a new OTP.",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        // OTP hợp lệ - đánh dấu đã sử dụng
        otpVerification.setIsUsed(true);
        otpRepository.save(otpVerification);

        log.info("OTP verified successfully for email: {}", email);
        return true;
    }
    private void checkResendCooldown(String email) {
        Optional<OtpVerification> lastOtpOpt =
                otpRepository.findFirstByEmailAndIsUsedFalseOrderByCreatedAtDesc(email);

        if (lastOtpOpt.isPresent()) {
            LocalDateTime lastOtpTime = lastOtpOpt.get().getCreatedAt();
            LocalDateTime cooldownEnd = lastOtpTime.plusSeconds(RESEND_COOLDOWN_SECONDS);

            if (LocalDateTime.now().isBefore(cooldownEnd)) {
                long remainingSeconds = Duration.between(LocalDateTime.now(), cooldownEnd).getSeconds();
                throw new HttpException(
                        String.format("Please wait %d seconds before requesting a new OTP", remainingSeconds),
                        HttpStatus.TOO_MANY_REQUESTS
                );
            }
        }
    }

    public long getRemainingCooldownSeconds(String email) {
        return otpRepository.findFirstByEmailAndIsUsedFalseOrderByCreatedAtDesc(email)
                .map(last -> {
                    LocalDateTime cooldownEnd = last.getCreatedAt().plusSeconds(RESEND_COOLDOWN_SECONDS);
                    return LocalDateTime.now().isBefore(cooldownEnd)
                            ? Duration.between(LocalDateTime.now(), cooldownEnd).getSeconds()
                            : 0L;
                })
                .orElse(0L);
    }


    private void incrementAttemptCount(String email) {
        Optional<OtpVerification> otpOpt =
                otpRepository.findFirstByEmailAndIsUsedFalseOrderByCreatedAtDesc(email);

        otpOpt.ifPresent(otp -> {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            otpRepository.save(otp);
        });
    }

    private void cleanupOldOtps(String email) {
        otpRepository.deleteByEmailAndExpiresAtBefore(email, LocalDateTime.now());
    }

    @Scheduled(fixedRate = 60000) // Chạy mỗi 5 phút
    public void cleanupExpiredData() {
        log.info("Starting cleanup of expired OTP and pending user data");

        LocalDateTime now = LocalDateTime.now();
        otpRepository.deleteExpiredOtps(now);
        pendingUserRepository.deleteExpiredPendingUsers(now);

        log.info("Completed cleanup of expired data");
    }


}