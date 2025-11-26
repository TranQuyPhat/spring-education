package com.example.springboot_education.controllers;

import com.example.springboot_education.annotations.CurrentUser;
import com.example.springboot_education.dtos.*;
import com.example.springboot_education.dtos.AuthDTOs.*;
import com.example.springboot_education.entities.Role;
import com.example.springboot_education.entities.UserRole;
import com.example.springboot_education.entities.UserRoleId;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.RoleJpaRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.services.AuthService;
import com.example.springboot_education.services.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsersJpaRepository usersJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final JwtService jwtService;

    private final AuthService authService;
//
//    @PostMapping("/login")
//    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) throws Exception {
//        LoginResponseDto result = this.authService.login(request);
//        return ResponseEntity.ok(result);
//    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return ResponseEntity.ok().body(Map.of("message", "Logout successful"));
    }

    @PostMapping("/google-login")
    public ResponseEntity<LoginResponseDto> googleLogin(@RequestBody @Valid GoogleLoginRequestDto request)
            throws Exception {
        LoginResponseDto result = this.authService.googleLogin(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/google-login-with-credential")
    public ResponseEntity<LoginResponseDto> googleLoginWithCredential(
            @RequestBody @Valid GoogleLoginWithCredentialRequestDto request) throws Exception {
        LoginResponseDto result = this.authService.googleLoginWithCredential(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/select-role")
    public ResponseEntity<?> selectRole(@RequestBody SelectRoleRequestDto request) {
        try {
            Users user = usersJpaRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getUserRoles().isEmpty()) {
                String accessToken = jwtService.generateAccessToken(user);
                LoginResponseDto response = LoginResponseDto.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .roles(user.getUserRoles().stream()
                                .map(ur -> ur.getRole() != null ? ur.getRole().getName() : "UNKNOWN")
                                .collect(Collectors.toList()))
                        .accessToken(accessToken)
                        .build();
                return ResponseEntity.ok(response);
            }

            // Lấy role từ DB
            Role role = roleJpaRepository.findByName(request.getRole().toLowerCase())
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            // Thêm UserRole mới trực tiếp vào list hiện tại
            UserRole userRole = new UserRole();
            userRole.setId(new UserRoleId(user.getId(), role.getId()));
            userRole.setUser(user);
            userRole.setRole(role);
            userRole.setEnabled(true);

            user.getUserRoles().add(userRole); // <-- thêm trực tiếp vào list hiện tại

            Users updatedUser = usersJpaRepository.save(user);

            String accessToken = jwtService.generateAccessToken(updatedUser);

            LoginResponseDto response = LoginResponseDto.builder()
                    .userId(updatedUser.getId())
                    .username(updatedUser.getUsername())
                    .email(updatedUser.getEmail())
                    .roles(updatedUser.getUserRoles().stream()
                            .map(ur -> ur.getRole() != null ? ur.getRole().getName() : "UNKNOWN")
                            .collect(Collectors.toList()))
                    .accessToken(accessToken)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Select role failed", "error", e.getMessage()));
        }
    }
    @PostMapping("/register/init")
    public ResponseEntity<ApiResponse<RegisterInitResponseDto>> initRegistration(
            @Valid @RequestBody RegisterRequestDto request) {


        RegisterInitResponseDto response = authService.registerInit(request);

        return ResponseEntity.ok(ApiResponse.<RegisterInitResponseDto>builder()
                .success(true)
                .message("OTP sent successfully")
                .data(response)
                .build());
    }

    // Bước 2: Xác thực OTP và hoàn tất đăng ký
    @PostMapping("/register/confirm")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> confirmRegistration(
            @Valid @RequestBody ConfirmRegistrationDto request) {


        RegisterResponseDto response = authService.confirmRegistration(request);

        return ResponseEntity.ok(ApiResponse.<RegisterResponseDto>builder()
                .success(true)
                .message("Registration completed successfully")
                .data(response)
                .build());
    }

    // Gửi lại OTP
    @PostMapping("/otp/resend")
    public ResponseEntity<ApiResponse<ResendOtpResponseDto>> resendOtp(
            @Valid @RequestBody ResendOtpRequestDto request) {


        ResendOtpResponseDto response = authService.resendOtp(request);

        return ResponseEntity.ok(ApiResponse.<ResendOtpResponseDto>builder()
                .success(true)
                .message("New OTP sent successfully")
                .data(response)
                .build());
    }

    // Login endpoint giữ nguyên
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {


        LoginResponseDto response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.<LoginResponseDto>builder()
                .success(true)
                .message("Login successful")
                .data(response)
                .build());
    }

    // Endpoint /me - Lấy thông tin user đang đăng nhập
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser(@CurrentUser Users currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not authenticated", null));
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", currentUser.getId());
        userData.put("username", currentUser.getUsername());
        userData.put("email", currentUser.getEmail());
        userData.put("fullName", currentUser.getFullName());
        userData.put("isEmailVerified", currentUser.getIsEmailVerified());
        
        // Lấy danh sách roles
        List<String> roles = currentUser.getUserRoles().stream()
                .map(ur -> ur.getRole() != null ? ur.getRole().getName() : "UNKNOWN")
                .collect(Collectors.toList());
        userData.put("roles", roles);

        return ResponseEntity.ok(new ApiResponse<>(true, "Current user info", userData));
    }

    // Endpoint kiểm tra trạng thái registration
    @GetMapping("/register/status/{email}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRegistrationStatus(
            @PathVariable @Email String email) {

        // Logic kiểm tra trạng thái pending user
        // Trả về thông tin như: có đang chờ xác thực không, thời gian hết hạn, etc.

        Map<String, Object> status = new HashMap<>();
        status.put("email", email);
        status.put("isPending", true); // Tính toán từ database
        status.put("expiresIn", 1500); // Seconds remaining

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(status)
                .build());
    }
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid Authorization header"));
            }

            String token = authHeader.substring(7);

            // Validate token format and expiry
            if (!jwtService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }

            // Extract username from token
            String username = jwtService.extractUsername(token);

            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token payload"));
            }

            // Check if user still exists and is active
            Users user = usersJpaRepository.findByEmail(username) // Changed from userService.findByEmail
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found"));
            }

            // Check if user is still active/enabled
//            if (!user.isEnabled()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(Map.of("error", "User account is disabled"));
//            }

            // Extract roles from token for better performance
            List<Map<String, Object>> tokenRoles = jwtService.extractRoles(token);
            List<String> roleNames = tokenRoles.stream()
                    .map(role -> (String) role.get("name"))
                    .collect(Collectors.toList());

            // Return success with user info
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("username", username);
            response.put("userId", user.getId());
            response.put("roles", roleNames);
            response.put("email", user.getEmail());

            // Optional: Add token expiration info
            long remainingTime = jwtService.getRemainingTime(token);
            response.put("expiresIn", remainingTime / 1000); // in seconds

            return ResponseEntity.ok(response);

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token has expired"));
        } catch (MalformedJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token format"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token signature"));
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    // Optional: Add refresh token endpoint if needed
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");

            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Refresh token is required"));
            }

            // For now, since you don't have refresh tokens implemented,
            // you can return an error or implement basic refresh logic
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(Map.of("error", "Refresh token not implemented yet"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Failed to refresh token"));
        }
    }
}
