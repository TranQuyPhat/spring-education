package com.example.springboot_education.untils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtTokenUtil {

    private SecretKey getSigningKey() {
        // Sử dụng CÙNG secret như trong JwtService
        String secretKey = "MIsMiHz45ATNS6elM6dQLfN6oQIBDSV+KbAc5PE3rlA=";
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
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
