package com.example.springboot_education.services.mail;

import com.example.springboot_education.entities.Users;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.repositories.UsersJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {

    private final UsersJpaRepository userRepository;
    private final OtpRedisService otpRedisService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String RESET_TOKEN_PREFIX = "reset_token:";
    private static final long RESET_TOKEN_TTL_MINUTES = 10; // token đặt lại mật khẩu còn hiệu lực 10 phút

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Gửi OTP để thay đổi mật khẩu (user đã đăng nhập)
     */
    public void sendChangePasswordOtp(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new HttpException("User not found", HttpStatus.NOT_FOUND));

        otpRedisService.sendChangePasswordOtp(email, user.getFullName());
        log.info("Change password OTP requested for user: {}", email);
    }

    /**
     * Thay đổi mật khẩu sau khi xác thực OTP (user đã đăng nhập)
     */
    @Transactional
    public void changePasswordWithOtp(String email, String otp, String oldPassword, String newPassword) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new HttpException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new HttpException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        if (!otpRedisService.verifyChangePasswordOtp(email, otp)) {
            throw new HttpException("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new HttpException("New password must be different from current password", HttpStatus.BAD_REQUEST);
        }

        validatePasswordPolicy(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // TODO: revoke refresh tokens của user nếu bạn lưu trong DB/Redis
        log.info("Password changed successfully for user: {}", email);
    }

    /**
     * Gửi OTP cho quên mật khẩu
     */
    public void sendForgotPasswordOtp(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new HttpException("User not found", HttpStatus.NOT_FOUND));

        otpRedisService.sendForgotPasswordOtp(email, user.getFullName());
        log.info("Forgot password OTP sent for user: {}", email);
    }

    /**
     * Xác thực OTP quên mật khẩu -> cấp resetToken tạm (TTL 10 phút)
     */
    public String verifyForgotPasswordOtpIssueToken(String email, String otp) {
        // Tìm user (để tránh lộ thông tin, vẫn thống nhất 404 nếu không tồn tại)
        userRepository.findByEmail(email)
                .orElseThrow(() -> new HttpException("User not found", HttpStatus.NOT_FOUND));

        if (!otpRedisService.verifyForgotPasswordOtp(email, otp)) {
            throw new HttpException("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        // Sinh reset token ngẫu nhiên, base64-url-safe
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String resetToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // Lưu resetToken -> email vào Redis
        String key = RESET_TOKEN_PREFIX + resetToken;
        redisTemplate.opsForValue().set(key, email, RESET_TOKEN_TTL_MINUTES, TimeUnit.MINUTES);

        log.info("Issued reset token for email {} with TTL {} minutes", email, RESET_TOKEN_TTL_MINUTES);
        return resetToken;
    }

    /**
     * Reset mật khẩu với resetToken tạm
     */
    @Transactional
    public void resetPasswordWithToken(String resetToken, String newPassword, String confirmPassword) {
        if (resetToken == null || resetToken.isBlank()) {
            throw new HttpException("Reset token is required", HttpStatus.BAD_REQUEST);
        }

        String key = RESET_TOKEN_PREFIX + resetToken;
        String email = redisTemplate.opsForValue().get(key);
        if (email == null) {
            throw new HttpException("Invalid or expired reset token", HttpStatus.BAD_REQUEST);
        }

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new HttpException("User not found", HttpStatus.NOT_FOUND));

        if (!newPassword.equals(confirmPassword)) {
            throw new HttpException("Password confirmation does not match", HttpStatus.BAD_REQUEST);
        }

        validatePasswordPolicy(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Xóa token sau khi dùng
        redisTemplate.delete(key);

        // TODO: revoke refresh tokens
        log.info("Password reset successfully for user: {}", email);
    }

    /**
     * Lấy thời gian cooldown còn lại cho việc gửi OTP thay đổi mật khẩu
     */
    public long getChangePasswordCooldown(String email) {
        return otpRedisService.getRemainingCooldown(email, "CHANGE_PASSWORD");
    }

    /**
     * Lấy thời gian cooldown còn lại cho việc gửi OTP quên mật khẩu
     */
    public long getForgotPasswordCooldown(String email) {
        return otpRedisService.getRemainingCooldown(email, "FORGOT_PASSWORD");
    }

    private void validatePasswordPolicy(String newPassword) {
        // Ví dụ chính sách tối thiểu – bạn có thể thay đổi theo nhu cầu
        if (newPassword.length() < 8) {
            throw new HttpException("Password must be at least 8 characters", HttpStatus.BAD_REQUEST);
        }
        // Có thể thêm: ít nhất 1 chữ in hoa, 1 chữ thường, 1 số, 1 ký tự đặc biệt...
        // if (!Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", newPassword)) { ... }
    }
}
