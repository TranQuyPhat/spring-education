package com.example.springboot_education.controllers;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.springboot_education.services.DashboardService;
import com.example.springboot_education.repositories.UsersJpaRepository;
import com.example.springboot_education.entities.Users;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*") 
public class DashboardClientController {

    private final DashboardService dashboardService;
    private final UsersJpaRepository usersJpaRepository;

    public DashboardClientController(DashboardService dashboardService, UsersJpaRepository usersJpaRepository) {
        this.dashboardService = dashboardService;
        this.usersJpaRepository = usersJpaRepository;
    }

    @GetMapping("/teacher")
    public ResponseEntity<?> getTeacherDashboard() {
        return resolveCurrentUser()
                .map(user -> {
                    boolean isTeacher = user.getUserRoles().stream()
                            .anyMatch(ur -> ur.getRole() != null && "teacher".equalsIgnoreCase(ur.getRole().getName()));
                    if (isTeacher) {
                        return ResponseEntity.ok(dashboardService.getTeacherDashboard(user.getId()));
                    }
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not a teacher");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated"));
    }

    @GetMapping("/student")
    public ResponseEntity<?> getStudentDashboard() {
        return resolveCurrentUser()
                .map(user -> {
                    boolean isStudent = user.getUserRoles().stream()
                            .anyMatch(ur -> ur.getRole() != null && "student".equalsIgnoreCase(ur.getRole().getName()));
                    if (isStudent) {
  var response = dashboardService.getStudentDashboard(user.getId());
                        System.out.println("StudentDashboardResponse = " + response); 
                        return ResponseEntity.ok(response);                    }
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not a student");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated"));
    }

    private Optional<Users> resolveCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) return Optional.empty();

            String username = auth.getName();
            return usersJpaRepository.findByUsernameWithRoles(username)
                    .or(() -> usersJpaRepository.findByEmailWithRoles(username));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}