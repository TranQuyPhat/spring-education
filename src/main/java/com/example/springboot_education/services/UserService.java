package com.example.springboot_education.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.springboot_education.annotations.LoggableAction; // Import annotation
// Xóa import ActivityLogCreateDTO
// import com.example.springboot_education.dtos.activitylogs.ActivityLogCreateDTO; 
import com.example.springboot_education.dtos.roleDTOs.RoleResponseDto;
import com.example.springboot_education.dtos.usersDTOs.CreateUserRequestDto;
import com.example.springboot_education.dtos.usersDTOs.UpdateUserRequestDto;
import com.example.springboot_education.dtos.usersDTOs.UserResponseDto;
import com.example.springboot_education.entities.UserRole;
import com.example.springboot_education.entities.UserRoleId;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.repositories.RoleJpaRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UsersJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;

    private UserResponseDto convertToDto(Users user) {
        List<RoleResponseDto> roles = user.getUserRoles().stream()
                .map(userRole -> new RoleResponseDto(
                        userRole.getRole().getId(),
                        userRole.getRole().getName(),
                        userRole.getRole().getCreatedAt(),
                        userRole.getRole().getUpdatedAt()
                ))
                .collect(Collectors.toList());

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .imageUrl(user.getImageUrl())
                .email(user.getEmail())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // Lấy toàn bộ user
    public List<UserResponseDto> getUsers() {
        List<Users> users = userJpaRepository.findAllUsersWithRoles();
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // Tạo user mới
    @LoggableAction(value = "CREATE", entity = "users", description = "Tạo user mới")
    public UserResponseDto createUser(CreateUserRequestDto dto) {
        if (this.userJpaRepository.existsByEmail(dto.getEmail())) {
            throw new HttpException("Email already exists: " + dto.getEmail(), HttpStatus.CONFLICT);
        }

        Users user = new Users();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setImageUrl(dto.getImageUrl());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            for (Integer roleId : dto.getRoles()) {
                var role = roleJpaRepository.findById(roleId)
                        .orElseThrow(() -> new HttpException("Role not found with id: " + roleId, HttpStatus.NOT_FOUND));

                var userRole = new UserRole();
                userRole.setId(new UserRoleId(user.getId(), role.getId()));
                userRole.setUser(user);
                userRole.setRole(role);

                user.getUserRoles().add(userRole);
            }
        }

        Users savedUser = userJpaRepository.save(user);

        // Xóa code ghi logs thủ công
        // activityLogService.log(...);

        return convertToDto(savedUser);
    }

    // GET USER BY ID
    public UserResponseDto getUserById(Integer id) {
        Users user = this.userJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDto(user);
    }

    // Cập nhật user
    @LoggableAction(value = "UPDATE", entity = "users", description = "Cập nhật user")
    public UserResponseDto updateUser(Integer id, UpdateUserRequestDto dto) {
        Users user = userJpaRepository.findById(id)
                .orElseThrow(() -> new HttpException("User not found with id: " + id, HttpStatus.NOT_FOUND));

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setImageUrl(dto.getImageUrl());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRoles() != null) {
            user.getUserRoles().clear();
            for (Integer roleId : dto.getRoles()) {
                var role = roleJpaRepository.findById(roleId)
                        .orElseThrow(() -> new HttpException("Role not found with id: " + roleId, HttpStatus.NOT_FOUND));

                var userRole = new UserRole();
                userRole.setId(new UserRoleId(user.getId(), role.getId()));
                userRole.setUser(user);
                userRole.setRole(role);

                user.getUserRoles().add(userRole);
            }
        }

        Users updatedUser = userJpaRepository.save(user);

        // Xóa code ghi logs thủ công
        // activityLogService.log(...);

        return convertToDto(updatedUser);
    }

    // Xoá user
    @Transactional
    @LoggableAction(value = "DELETE", entity = "users", description = "Xóa user")
    public void deleteUser(Integer id) {
        Users user = userJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Xóa code ghi logs thủ công
        // activityLogService.log(...);

        userJpaRepository.delete(user);
    }

    public boolean isCurrentUser(Integer userId, String currentUserEmail) {
        Optional<Users> user = userJpaRepository.findById(userId);
        return user.isPresent() && user.get().getEmail().equals(currentUserEmail);
    }
}