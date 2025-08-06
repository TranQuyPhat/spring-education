package com.example.springboot_education.services;

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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    return LoginResponseDto.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .accessToken(accessToken)
            .build();
}

}
