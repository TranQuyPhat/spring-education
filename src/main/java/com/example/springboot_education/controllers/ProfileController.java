package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.usersDTOs.ChangePasswordRequestDto;
import com.example.springboot_education.dtos.usersDTOs.UpdateProfileRequestDto;
import com.example.springboot_education.dtos.usersDTOs.UserResponseDto;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus; 

import java.util.Map;
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final UserService userService;

    // Return current user profile
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Users user = userService.getUserEntityByUsername(username);
        return ResponseEntity.ok(userService.convertToDtoPublic(user));
    }

    // Update profile (fullName and imageUrl)
    @PatchMapping("")
    public ResponseEntity<UserResponseDto> updateProfile(@RequestBody UpdateProfileRequestDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Users user = userService.getUserEntityByUsername(username);
        return ResponseEntity.ok(userService.updateProfileByUser(user.getId(), dto));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequestDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Users user = userService.getUserEntityByUsername(username);
        userService.changePassword(user.getId(), dto.getOldPassword(), dto.getNewPassword());
        return ResponseEntity.ok().build();
    }

@PostMapping("/avatar")
@Transactional
public ResponseEntity<UserResponseDto> uploadAvatar(@RequestBody Map<String, String> payload) {
    try {
        String base64 = payload.get("imageUrl");
        if (base64 == null || base64.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Validate Base64 format
        if (!base64.startsWith("data:image/")) {
            return ResponseEntity.badRequest().build();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Users user = userService.getUserEntityByUsername(username);

        return ResponseEntity.ok(
            userService.updateProfileByUser(
                user.getId(),
                new UpdateProfileRequestDto(user.getFullName(), base64)
            )
        );
    } catch (Exception e) {
        log.error("Error uploading avatar: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}


}
