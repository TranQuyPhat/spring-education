package com.example.springboot_education.controllers.dashboard;

import com.example.springboot_education.dtos.dashboard.student.RecentScoreDTO;
import com.example.springboot_education.dtos.dashboard.student.SubjectGradeDTO;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.services.dashboard.student.StudentScoreService;
import com.example.springboot_education.untils.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentScoreController {

    @Autowired
    private StudentScoreService studentScoreService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // API lấy 5 điểm gần nhất
    @GetMapping("/recent-scores")
    public ResponseEntity<?> getRecentScores(HttpServletRequest request) {
        try {
            // Lấy token từ header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new HttpException("Authorization header missing or invalid format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            // Validate token trước
            if (!jwtTokenUtil.validateToken(token)) {
                throw new HttpException("Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            Integer studentId = jwtTokenUtil.getUserIdFromToken(token);
            if (studentId == null) {
                throw new HttpException("Cannot extract user ID from token", HttpStatus.BAD_REQUEST);
            }

            List<RecentScoreDTO> recentScores = studentScoreService.getRecentScores(studentId);
            return ResponseEntity.ok(recentScores);

        } catch (EntityNotFoundException e) {
            throw new HttpException(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (HttpException e) {
            throw e; 
        } catch (Exception e) {
            throw new HttpException("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // API lấy tất cả kết quả của các lớp
    // @GetMapping("/class-results")
    // public ResponseEntity<?> getAllClassResults(HttpServletRequest request) {
    // try {
    // // Lấy token từ header
    // String authHeader = request.getHeader("Authorization");
    // if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    // return createErrorResponse("Authorization header missing or invalid format",
    // HttpStatus.UNAUTHORIZED);
    // }
    //
    // String token = authHeader.substring(7);
    //
    // // Validate token trước
    // if (!jwtTokenUtil.validateToken(token)) {
    // return createErrorResponse("Invalid or expired token",
    // HttpStatus.UNAUTHORIZED);
    // }
    //
    // Integer studentId = jwtTokenUtil.getUserIdFromToken(token);
    // if (studentId == null) {
    // return createErrorResponse("Cannot extract user ID from token",
    // HttpStatus.BAD_REQUEST);
    // }
    //
    // List<ClassResultDTO> classResults =
    // studentScoreService.getAllClassResults(studentId);
    // return ResponseEntity.ok(classResults);
    //
    // } catch (Exception e) {
    // return createErrorResponse("Error processing request: " + e.getMessage(),
    // HttpStatus.INTERNAL_SERVER_ERROR);
    // }
    // }
    @GetMapping("/grades")
    public ResponseEntity<?> getStudentGrades(HttpServletRequest request) {
        try {
            // Lấy token từ header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new HttpException("Authorization header missing or invalid format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            // Validate token trước
            if (!jwtTokenUtil.validateToken(token)) {
                throw new HttpException("Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            Integer studentId = jwtTokenUtil.getUserIdFromToken(token);
            if (studentId == null) {
                throw new HttpException("Cannot extract user ID from token", HttpStatus.BAD_REQUEST);
            }

            List<SubjectGradeDTO> grades = studentScoreService.getAllClassResults(studentId);
            return ResponseEntity.ok(grades);

        } catch (Exception e) {
            throw new HttpException("Error processing request: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // API test token để debug
    @GetMapping("/test-token")
    public ResponseEntity<?> testToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");

            Map<String, Object> response = new HashMap<>();
            response.put("authHeader", authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                response.put("tokenLength", token.length());
                response.put("tokenPreview", token.substring(0, Math.min(token.length(), 50)) + "...");

                try {
                    boolean isValid = jwtTokenUtil.validateToken(token);
                    response.put("isValidToken", isValid);

                    if (isValid) {
                        Integer userId = jwtTokenUtil.getUserIdFromToken(token);
                        response.put("userId", userId);
                        response.put("extractionSuccess", true);
                    }
                } catch (Exception e) {
                    response.put("tokenError", e.getMessage());
                    response.put("tokenErrorType", e.getClass().getSimpleName());
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new HttpException("Error testing token: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method tạo error response
    // private ResponseEntity<Map<String, Object>> createErrorResponse(String
    // message, HttpStatus status) {
    // Map<String, Object> errorResponse = new HashMap<>();
    // errorResponse.put("error", message);
    // errorResponse.put("status", status.value());
    // errorResponse.put("timestamp", System.currentTimeMillis());
    // return ResponseEntity.status(status).body(errorResponse);
    // }
}