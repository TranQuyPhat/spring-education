package com.example.springboot_education.services;

import com.example.springboot_education.dtos.AuthDTOs.*;
import com.example.springboot_education.dtos.ConfirmRegistrationDto;
import com.example.springboot_education.dtos.RegisterInitResponseDto;
import com.example.springboot_education.dtos.ResendOtpRequestDto;
import com.example.springboot_education.dtos.ResendOtpResponseDto;
import com.example.springboot_education.entities.*;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.repositories.PendingUserRepository;
import com.example.springboot_education.repositories.RoleJpaRepository;
import com.example.springboot_education.repositories.UserRoleRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.services.mail.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final JwtService jwtService;
    private final UsersJpaRepository userJpaRepository;
    private final RoleJpaRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PendingUserRepository pendingUserRepository;
    private final OtpService otpService;
    @Transactional
    public RegisterInitResponseDto registerInit(RegisterRequestDto request) {
        // Kiểm tra email đã tồn tại
        if (userJpaRepository.existsByEmail(request.getEmail())) {
            throw new HttpException("Email already exists", HttpStatus.CONFLICT);
        }

        // Kiểm tra username đã tồn tại
        if (userJpaRepository.existsByUsername(request.getUsername())) {
            throw new HttpException("Username already exists", HttpStatus.CONFLICT);
        }

        // Kiểm tra role có tồn tại
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new HttpException("Role not found", HttpStatus.BAD_REQUEST));

        // Xóa pending user cũ nếu có
        pendingUserRepository.deleteByEmail(request.getEmail());

        // Tạo pending user mới
        PendingUser pendingUser = PendingUser.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roleName(request.getRoleName())
                .build();

        pendingUserRepository.save(pendingUser);

        // Gửi OTP
        otpService.sendOtp(request.getEmail(), request.getFullName());

        return RegisterInitResponseDto.builder()
                .message("Registration initiated. Please verify your email with the OTP sent.")
                .email(request.getEmail())
                .build();
    }

    // Bước 2: Xác thực OTP và hoàn tất đăng ký
    @Transactional
    public RegisterResponseDto confirmRegistration(ConfirmRegistrationDto request) {
        // Xác thực OTP
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            throw new HttpException("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        // Lấy thông tin pending user
        PendingUser pendingUser = pendingUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new HttpException("Registration session not found", HttpStatus.NOT_FOUND));

        // Kiểm tra pending user chưa hết hạn
        if (pendingUser.getExpiresAt().isBefore(LocalDateTime.now())) {
            pendingUserRepository.deleteByEmail(request.getEmail());
            throw new HttpException("Registration session expired. Please register again.", HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra lại email và username (có thể đã được đăng ký trong lúc chờ xác thực)
        if (userJpaRepository.existsByEmail(pendingUser.getEmail())) {
            pendingUserRepository.deleteByEmail(request.getEmail());
            throw new HttpException("Email already exists", HttpStatus.CONFLICT);
        }

        if (userJpaRepository.existsByUsername(pendingUser.getUsername())) {
            pendingUserRepository.deleteByEmail(request.getEmail());
            throw new HttpException("Username already exists", HttpStatus.CONFLICT);
        }

        // Lấy role
        Role role = roleRepository.findByName(pendingUser.getRoleName())
                .orElseThrow(() -> new HttpException("Role not found", HttpStatus.BAD_REQUEST));

        // Tạo user thật
        Users newUser = new Users();
        newUser.setUsername(pendingUser.getUsername());
        newUser.setEmail(pendingUser.getEmail());
        newUser.setFullName(pendingUser.getFullName());
        newUser.setPassword(pendingUser.getPassword()); // Đã được encode
        newUser.setIsEmailVerified(true); // Đánh dấu email đã xác thực

        Users savedUser = userJpaRepository.save(newUser);

        // Tạo user role
        UserRole userRole = new UserRole();
        userRole.setId(new UserRoleId(savedUser.getId(), role.getId()));
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        pendingUserRepository.deleteByEmail(request.getEmail());

        String token = jwtService.generateAccessToken(savedUser);


        return RegisterResponseDto.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .accessToken(token)
                .build();
    }
    @Transactional
    public ResendOtpResponseDto resendOtp(ResendOtpRequestDto request) {
        // Kiểm tra pending user có tồn tại
        PendingUser pendingUser = pendingUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new HttpException("No pending registration found for this email", HttpStatus.NOT_FOUND));

        // Kiểm tra pending user chưa hết hạn
        if (pendingUser.getExpiresAt().isBefore(LocalDateTime.now())) {
            pendingUserRepository.deleteByEmail(request.getEmail());
            throw new HttpException("Registration session expired. Please register again.", HttpStatus.BAD_REQUEST);
        }

        // Gửi OTP mới
        otpService.sendOtp(request.getEmail(), pendingUser.getFullName());

        return ResendOtpResponseDto.builder()
                .message("New OTP sent successfully")
                .email(request.getEmail())
                .cooldownSeconds(otpService.getRemainingCooldownSeconds(request.getEmail()))
                .build();
    }
    public RegisterResponseDto register(RegisterRequestDto request) {
        if (userJpaRepository.existsByEmail(request.getEmail())) {
            throw new HttpException("Email already exists", HttpStatus.CONFLICT);
        }

        if (userJpaRepository.existsByUsername(request.getUsername())) {
            throw new HttpException("Username already exists", HttpStatus.CONFLICT);
        }

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new HttpException("Role not found", HttpStatus.BAD_REQUEST));

        Users newUser = new Users();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setFullName(request.getFullName());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        Users savedUser = userJpaRepository.save(newUser);

        UserRole userRole = new UserRole();
        userRole.setId(new UserRoleId(savedUser.getId(), role.getId()));
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        String token = jwtService.generateAccessToken(savedUser);

        return RegisterResponseDto.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .accessToken(token)
                .build();
    }

    // ✅ Đăng nhập
    public LoginResponseDto login(LoginRequestDto request) {
        Users user = null;

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user = userJpaRepository.findByEmailWithRoles(request.getEmail())
                    .orElseThrow(() -> new HttpException("Invalid email or password", HttpStatus.UNAUTHORIZED));
        } else if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            user = userJpaRepository.findByUsernameWithRoles(request.getUsername())
                    .orElseThrow(() -> new HttpException("Invalid username or password", HttpStatus.UNAUTHORIZED));
        } else {
            throw new HttpException("Username or Email is required", HttpStatus.BAD_REQUEST);
        } 

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new HttpException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtService.generateAccessToken(user);

        List<String> roles = user.getUserRoles()
                .stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());

        return LoginResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .imageUrl(user.getImageUrl())
                .accessToken(accessToken)
                .roles(roles)
                .build();
    }

    public LoginResponseDto googleLogin(GoogleLoginRequestDto request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new HttpException("Email is required", HttpStatus.BAD_REQUEST);
        }

        Optional<Users> userOptional = userJpaRepository.findByEmail(request.getEmail());
        Users user;

        if (userOptional.isEmpty()) {
            // Tạo user mới
            user = new Users();
            user.setEmail(request.getEmail());
            user.setUsername(
                    request.getEmail().length() > 50 ? request.getEmail().substring(0, 50) : request.getEmail());
            user.setPassword(UUID.randomUUID().toString()); // password ngẫu nhiên
            user.setFullName(request.getName());
            user.setImageUrl(request.getImageUrl());

            // Lưu user trước để có ID
            Users savedUser = userJpaRepository.save(user);

            // Lấy role từ DB (ID = 3)
            Role role = roleRepository.findById(3)
                    .orElseThrow(() -> new HttpException("Role not found", HttpStatus.INTERNAL_SERVER_ERROR));

            UserRole userRole = new UserRole();
            userRole.setId(new UserRoleId(savedUser.getId(), role.getId()));
            userRole.setUser(savedUser);
            userRole.setRole(role);
            userRole.setEnabled(true);

            savedUser.setUserRoles(List.of(userRole));

            // Lưu lại user kèm role
            user = userJpaRepository.save(savedUser);
        } else {
            user = userOptional.get();
        }

        // Generate access token
        String accessToken = jwtService.generateAccessToken(user);

        return LoginResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .accessToken(accessToken)
                .roles(user.getUserRoles()
                        .stream()
                        .map(ur -> ur.getRole().getName())
                        .collect(Collectors.toList()))
                .build();
    }

    public LoginResponseDto googleLoginWithCredential(GoogleLoginWithCredentialRequestDto request) {
    if (request.getCredential() == null || request.getCredential().isEmpty()) {
        throw new HttpException("Credential is required", HttpStatus.BAD_REQUEST);
    }

    Map<String, Object> payload;
    try {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getCredential();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new HttpException("Invalid Google token", HttpStatus.UNAUTHORIZED);
        }
        payload = response.getBody();
    } catch (Exception e) {
        throw new HttpException("Google token verification failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    String email = (String) payload.get("email");
    if (email == null || email.isEmpty()) {
        throw new HttpException("Email not found in token", HttpStatus.UNAUTHORIZED);
    }

    String iss = (String) payload.get("iss");
    if (!"https://accounts.google.com".equals(iss) && !"accounts.google.com".equals(iss)) {
        throw new HttpException("Invalid token issuer", HttpStatus.UNAUTHORIZED);
    }

    long exp = Long.parseLong(payload.get("exp").toString());
    if (exp < System.currentTimeMillis() / 1000) {
        throw new HttpException("Token expired", HttpStatus.UNAUTHORIZED);
    }

    Users user = userJpaRepository.findByEmail(email).orElseGet(() -> {
    Users u = new Users();
    u.setEmail(email);
    u.setUsername(email.length() > 50 ? email.substring(0, 50) : email);
    u.setPassword(UUID.randomUUID().toString());
    u.setFullName((String) payload.get("name"));
    u.setImageUrl((String) payload.get("picture"));
    u.setUserRoles(new ArrayList<>());
    return userJpaRepository.save(u);
});


    boolean hasRole = user.getUserRoles() != null && !user.getUserRoles().isEmpty();

    String accessToken = jwtService.generateAccessToken(user);

    return LoginResponseDto.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .accessToken(accessToken)
            .roles(user.getUserRoles()
                    .stream()
                    .map(ur -> ur.getRole().getName())
                    .collect(Collectors.toList()))
            .requireRoleSelection(!hasRole) // Nếu chưa có role thì frontend biết
            .build();
}

}

