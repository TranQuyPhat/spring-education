package com.example.springboot_education.services;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.springboot_education.annotations.LoggableAction;
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

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UsersJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;

    private byte[] decodeBase64Image(String base64) {
        if (base64.contains(",")) {
            base64 = base64.split(",")[1];
        }
        return Base64.getDecoder().decode(base64);
    }

    private UserResponseDto convertToDto(Users user) {
        List<RoleResponseDto> roles = user.getUserRoles().stream()
                .map(userRole -> new RoleResponseDto(
                        userRole.getRole().getId(),
                        userRole.getRole().getName(),
                        userRole.getRole().getCreatedAt(),
                        userRole.getRole().getUpdatedAt()))
                .collect(Collectors.toList());

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarBase64(
                        user.getAvatar() != null
                                ? "data:image/png;base64," + Base64.getEncoder().encodeToString(user.getAvatar())
                                : null)
                .email(user.getEmail())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // public conversion for controllers
    public UserResponseDto convertToDtoPublic(Users user) {
        return convertToDto(user);
    }

    public Users getUserEntityByEmail(String email) {
        return userJpaRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public Users getUserEntityByUsername(String username) {
        return userJpaRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    // Update profile for currently authenticated user
    public UserResponseDto updateProfileByUser(
            Integer id,
            com.example.springboot_education.dtos.usersDTOs.UpdateProfileRequestDto dto) {
        Users user = userJpaRepository.findById(id)
                .orElseThrow(() -> new HttpException("User not found with id: " + id, HttpStatus.NOT_FOUND));

        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }

        // Nếu client gửi base64 thì decode rồi lưu vào avatar
        if (dto.getAvatarBase64() != null) {
            try {
                String base64 = dto.getAvatarBase64();
                if (base64.contains(",")) {
                    base64 = base64.split(",")[1];
                }
                user.setAvatar(Base64.getDecoder().decode(base64));
            } catch (IllegalArgumentException e) {
                throw new HttpException("Invalid base64 image", HttpStatus.BAD_REQUEST);
            }
        }

        Users updated = userJpaRepository.save(user);
        return convertToDto(updated);
    }

    public void changePassword(Integer id, String oldPassword, String newPassword) {
        Users user = userJpaRepository.findById(id)
                .orElseThrow(() -> new HttpException("User not found with id: " + id, HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new HttpException("Old password is incorrect", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userJpaRepository.save(user);
    }

    // Lấy toàn bộ user
    public List<UserResponseDto> getUsers() {
        List<Users> users = userJpaRepository.findAllUsersWithRoles();
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // Create new user
    @LoggableAction(value = "CREATE", entity = "users", description = "Create new user")
    public UserResponseDto createUser(CreateUserRequestDto dto) {
        if (this.userJpaRepository.existsByEmail(dto.getEmail())) {
            throw new HttpException("Email already exists: " + dto.getEmail(), HttpStatus.CONFLICT);
        }

        Users user = new Users();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());

        if (dto.getAvatarBase64() != null && !dto.getAvatarBase64().isEmpty()) {
            user.setAvatar(decodeBase64Image(dto.getAvatarBase64()));
        }

        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            for (Integer roleId : dto.getRoles()) {
                var role = roleJpaRepository.findById(roleId)
                        .orElseThrow(
                                () -> new HttpException("Role not found with id: " + roleId, HttpStatus.NOT_FOUND));

                var userRole = new UserRole();
                userRole.setId(new UserRoleId(user.getId(), role.getId()));
                userRole.setUser(user);
                userRole.setRole(role);

                user.getUserRoles().add(userRole);
            }
        }

        Users savedUser = userJpaRepository.save(user);

        return convertToDto(savedUser);
    }

    // GET USER BY ID
    public UserResponseDto getUserById(Integer id) {
        Users user = this.userJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDto(user);
    }

    public Users getUserEntityById(Integer id) {
        return userJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Update user
    @LoggableAction(value = "UPDATE", entity = "users", description = "Update user")
    public UserResponseDto updateUser(Integer id, UpdateUserRequestDto dto) {
        Users user = userJpaRepository.findById(id)
                .orElseThrow(() -> new HttpException("User not found with id: " + id, HttpStatus.NOT_FOUND));

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        if (dto.getAvatarBase64() != null && !dto.getAvatarBase64().isEmpty()) {
            user.setAvatar(decodeBase64Image(dto.getAvatarBase64()));
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRoles() != null) {
            user.getUserRoles().clear();
            for (Integer roleId : dto.getRoles()) {
                var role = roleJpaRepository.findById(roleId)
                        .orElseThrow(
                                () -> new HttpException("Role not found with id: " + roleId, HttpStatus.NOT_FOUND));

                var userRole = new UserRole();
                userRole.setId(new UserRoleId(user.getId(), role.getId()));
                userRole.setUser(user);
                userRole.setRole(role);

                user.getUserRoles().add(userRole);
            }
        }

        Users updatedUser = userJpaRepository.save(user);

        return convertToDto(updatedUser);
    }

    // Xoá user
    @Transactional
    @LoggableAction(value = "DELETE", entity = "users", description = "Delete user")
    public void deleteUser(Integer id) {
        Users user = userJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userJpaRepository.delete(user);
    }

    public boolean isCurrentUser(Integer userId, String currentUserEmail) {
        Optional<Users> user = userJpaRepository.findById(userId);
        return user.isPresent() && user.get().getEmail().equals(currentUserEmail);
    }

    public String getFullName(Integer userId) {
        return userJpaRepository.findById(userId)
                .map(Users::getFullName)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với ID: " + userId));
    }

    public UserResponseDto updateAvatar(Integer userId, MultipartFile file) throws IOException {
        Users user = userJpaRepository.findById(userId).orElseThrow();
        user.setAvatar(file.getBytes()); // lưu vào DB dạng BLOB
        userJpaRepository.save(user);
        return convertToDtoPublic(user);
    }

    public String getAvatarBase64(Integer userId) {
        Users user = userJpaRepository.findById(userId).orElseThrow();
        if (user.getAvatar() == null)
            return null;
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(user.getAvatar());
    }

}