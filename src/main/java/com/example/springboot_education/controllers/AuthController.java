package com.example.springboot_education.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot_education.dtos.AuthDTOs.GoogleLoginRequestDto;
import com.example.springboot_education.dtos.AuthDTOs.GoogleLoginWithCredentialRequestDto;
import com.example.springboot_education.dtos.AuthDTOs.LoginRequestDto;
import com.example.springboot_education.dtos.AuthDTOs.LoginResponseDto;
import com.example.springboot_education.dtos.AuthDTOs.RegisterRequestDto;
import com.example.springboot_education.dtos.AuthDTOs.RegisterResponseDto;
import com.example.springboot_education.dtos.AuthDTOs.SelectRoleRequestDto;
import com.example.springboot_education.entities.Role;
import com.example.springboot_education.entities.UserRole;
import com.example.springboot_education.entities.UserRoleId;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.repositories.RoleJpaRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.services.AuthService;
import com.example.springboot_education.services.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsersJpaRepository usersJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final JwtService jwtService;

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) throws Exception {
        LoginResponseDto result = this.authService.login(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterRequestDto request) {
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

}
