package com.example.springboot_education.untils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class JwtTokenUtil {

    private SecretKey getSigningKey() {
        // Sử dụng CÙNG secret như trong JwtService
        String secretKey = "MIsMiHz45ATNS6elM6dQLfN6oQIBDSV+KbAc5PE3rlA=";
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            Object rolesObj = claims.get("roles");

            List<String> roleNames = new ArrayList<>();

            if (rolesObj instanceof List<?>) {
                for (Object role : (List<?>) rolesObj) {
                    if (role instanceof Map<?, ?> map) {
                        Object name = map.get("name");
                        if (name instanceof String) {
                            roleNames.add((String) name);
                        }
                    }
                }
            }

            return roleNames;

        } catch (Exception e) {
            throw new RuntimeException("Error extracting roles from token: " + e.getMessage(), e);
        }
    }

    public List<String> getRoleNamesFromToken(String token) {
        Claims claims = getClaims(token);

        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?> rolesList) {
            return rolesList.stream()
                    .filter(Map.class::isInstance)
                    .map(roleMap -> ((Map<?, ?>) roleMap).get("name"))
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }

        return List.of(); // Không có role
    }
    public Integer getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Lấy user ID từ claim "id" 
            Object idObj = claims.get("id");
            if (idObj == null) {
                // Fallback: thử lấy từ subject nếu không có field "id"
                String subject = claims.getSubject();
                return Integer.valueOf(subject);
            }

            // Convert id về Integer
            if (idObj instanceof Number) {
                return ((Number) idObj).intValue();
            } else if (idObj instanceof String) {
                return Integer.valueOf((String) idObj);
            } else {
                throw new RuntimeException("Invalid ID format in token");
            }

        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired token: " + e.getMessage(), e);
        }
    }

    // Phương thức validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
