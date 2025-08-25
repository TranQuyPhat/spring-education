package com.example.springboot_education.services;

import com.example.springboot_education.dtos.AuthDTOs.GoogleLoginRequestDto;
import com.example.springboot_education.dtos.AuthDTOs.GoogleLoginWithCredentialRequestDto;
import com.example.springboot_education.dtos.AuthDTOs.LoginRequestDto;
import com.example.springboot_education.dtos.AuthDTOs.LoginResponseDto;
import com.example.springboot_education.dtos.AuthDTOs.RegisterRequestDto;
import com.example.springboot_education.dtos.AuthDTOs.RegisterResponseDto;
import com.example.springboot_education.entities.Role;
import com.example.springboot_education.entities.UserRole;
import com.example.springboot_education.entities.UserRoleId;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.repositories.RoleJpaRepository;
import com.example.springboot_education.repositories.UserRoleRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final JwtService jwtService;
    private final UsersJpaRepository userJpaRepository;
    private final RoleJpaRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Đăng ký
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

