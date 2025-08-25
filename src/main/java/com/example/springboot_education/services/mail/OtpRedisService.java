package com.example.springboot_education.services.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRE_MINUTES = 5; // OTP hết hạn sau 5 phút
    private static final int RESEND_COOLDOWN_SECONDS = 60; // Cooldown 1 phút giữa các lần gửi
    private static final int MAX_ATTEMPTS = 5; // Tối đa 5 lần thử sai

    // Key patterns cho Redis
    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String COOLDOWN_KEY_PREFIX = "otp_cooldown:";
    private static final String ATTEMPTS_KEY_PREFIX = "otp_attempts:";

    /**
     * Tạo OTP ngẫu nhiên 6 số
     */
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Gửi OTP cho việc đổi mật khẩu
     */
    public void sendChangePasswordOtp(String email, String fullName) {
        String purpose = "CHANGE_PASSWORD";
        checkCooldown(email, purpose);

        String otp = generateOtp();
        String otpKey = buildOtpKey(email, purpose);

        // Lưu OTP vào Redis với thời hạn 5 phút
        redisTemplate.opsForValue().set(otpKey, otp, OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // Set cooldown 1 phút
        setCooldown(email, purpose);

        // Reset attempt count
        resetAttempts(email, purpose);

        // Gửi email
        emailService.sendChangePasswordOtpEmail(email, otp, fullName);

        log.info("Change password OTP sent to email: {}", email);
    }

    /**
     * Gửi OTP cho việc quên mật khẩu
     */
    public void sendForgotPasswordOtp(String email, String fullName) {
        String purpose = "FORGOT_PASSWORD";
        checkCooldown(email, purpose);

        String otp = generateOtp();
        String otpKey = buildOtpKey(email, purpose);

        // Lưu OTP vào Redis với thời hạn 5 phút
        redisTemplate.opsForValue().set(otpKey, otp, OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // Set cooldown 1 phút
        setCooldown(email, purpose);

        // Reset attempt count
        resetAttempts(email, purpose);

        // Gửi email
        emailService.sendForgotPasswordOtpEmail(email, otp, fullName);

        log.info("Forgot password OTP sent to email: {}", email);
    }

    /**
     * Xác thực OTP cho đổi mật khẩu
     */
    public boolean verifyChangePasswordOtp(String email, String otp) {
        return verifyOtp(email, otp, "CHANGE_PASSWORD");
    }

    /**
     * Xác thực OTP cho quên mật khẩu
     */
    public boolean verifyForgotPasswordOtp(String email, String otp) {
        return verifyOtp(email, otp, "FORGOT_PASSWORD");
    }

    /**
     * Xác thực OTP chung
     */
    private boolean verifyOtp(String email, String otp, String purpose) {
        String otpKey = buildOtpKey(email, purpose);
        String attemptsKey = buildAttemptsKey(email, purpose);

        // Kiểm tra số lần thử
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= MAX_ATTEMPTS) {
            log.warn("Too many OTP attempts for email: {} and purpose: {}", email, purpose);
            return false;
        }

        // Lấy OTP từ Redis
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            log.warn("OTP not found or expired for email: {} and purpose: {}", email, purpose);
            incrementAttempts(email, purpose);
            return false;
        }

        if (!storedOtp.equals(otp)) {
            log.warn("Invalid OTP for email: {} and purpose: {}", email, purpose);
            incrementAttempts(email, purpose);
            return false;
        }

        // OTP hợp lệ - xóa khỏi Redis
        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptsKey);

        log.info("OTP verified successfully for email: {} and purpose: {}", email, purpose);
        return true;
    }

    /**
     * Kiểm tra cooldown
     */
    private void checkCooldown(String email, String purpose) {
        String cooldownKey = buildCooldownKey(email, purpose);

        if (redisTemplate.hasKey(cooldownKey)) {
            Long ttl = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
            throw new RuntimeException(
                    String.format("Please wait %d seconds before requesting a new OTP", ttl != null ? ttl : 0)
            );
        }
    }

    /**
     * Set cooldown
     */
    private void setCooldown(String email, String purpose) {
        String cooldownKey = buildCooldownKey(email, purpose);
        redisTemplate.opsForValue().set(cooldownKey, "1", RESEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Tăng số lần thử
     */
    private void incrementAttempts(String email, String purpose) {
        String attemptsKey = buildAttemptsKey(email, purpose);
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        redisTemplate.opsForValue().set(attemptsKey, String.valueOf(attempts + 1),
                OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Reset số lần thử
     */
    private void resetAttempts(String email, String purpose) {
        String attemptsKey = buildAttemptsKey(email, purpose);
        redisTemplate.delete(attemptsKey);
    }

    /**
     * Lấy thời gian cooldown còn lại
     */
    public long getRemainingCooldown(String email, String purpose) {
        String cooldownKey = buildCooldownKey(email, purpose);
        Long ttl = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    // Helper methods để build Redis keys
    private String buildOtpKey(String email, String purpose) {
        return OTP_KEY_PREFIX + purpose.toLowerCase() + ":" + email;
    }

    private String buildCooldownKey(String email, String purpose) {
        return COOLDOWN_KEY_PREFIX + purpose.toLowerCase() + ":" + email;
    }

    private String buildAttemptsKey(String email, String purpose) {
        return ATTEMPTS_KEY_PREFIX + purpose.toLowerCase() + ":" + email;
    }
}