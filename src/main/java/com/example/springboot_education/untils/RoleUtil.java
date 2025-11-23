package com.example.springboot_education.untils;

import com.example.springboot_education.entities.Users;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class để check quyền của user một cách an toàn
 * Tránh việc so sánh string trực tiếp giúp dễ bảo trì trong tương lai
 */
public class RoleUtil {

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_STUDENT = "student";

    /**
     * Check xem user có role cụ thể không (case-insensitive)
     */
    public static boolean hasRole(Users user, String roleName) {
        if (user == null || user.getUserRoles() == null) {
            return false;
        }
        return user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole() != null &&
                        roleName.equalsIgnoreCase(ur.getRole().getName()));
    }

    /**
     * Check xem user có bất kỳ role nào trong danh sách không
     */
    public static boolean hasAnyRole(Users user, String... roleNames) {
        if (user == null || user.getUserRoles() == null) {
            return false;
        }
        Set<String> roles = Arrays.stream(roleNames)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole() != null &&
                        roles.contains(ur.getRole().getName().toLowerCase()));
    }

    /**
     * Check xem user có tất cả các role trong danh sách không
     */
    public static boolean hasAllRoles(Users user, String... roleNames) {
        if (user == null || user.getUserRoles() == null) {
            return false;
        }
        Set<String> requiredRoles = Arrays.stream(roleNames)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> userRoles = user.getUserRoles().stream()
                .filter(ur -> ur.getRole() != null)
                .map(ur -> ur.getRole().getName().toLowerCase())
                .collect(Collectors.toSet());

        return userRoles.containsAll(requiredRoles);
    }

    /**
     * Check xem user có quyền ADMIN
     */
    public static boolean isAdmin(Users user) {
        return hasRole(user, ROLE_ADMIN);
    }

    /**
     * Check xem user có quyền TEACHER
     */
    public static boolean isTeacher(Users user) {
        return hasRole(user, ROLE_TEACHER);
    }

    /**
     * Check xem user có quyền STUDENT
     */
    public static boolean isStudent(Users user) {
        return hasRole(user, ROLE_STUDENT);
    }

    /**
     * Check xem user có quyền TEACHER hoặc ADMIN
     */
    public static boolean isTeacherOrAdmin(Users user) {
        return hasAnyRole(user, ROLE_TEACHER, ROLE_ADMIN);
    }
}
