package com.example.springboot_education.services;

import com.example.springboot_education.entities.Role;
import com.example.springboot_education.entities.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private SecretKey getSigningKey() {
        String secretKey = "MIsMiHz45ATNS6elM6dQLfN6oQIBDSV+KbAc5PE3rlA=";
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String createToken(Map<String, Object> claims, String subject, Integer expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateAccessToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("type", "access_token");

        List<Map<String, Object>> roles = user.getUserRoles().stream()
                .map(userRole -> {
                    Role role = userRole.getRole();
                    Map<String, Object> roleInfo = new HashMap<>();
                    roleInfo.put("id", role.getId());
                    roleInfo.put("name", role.getName());
                    return roleInfo;
                })
                .collect(Collectors.toList());

        claims.put("roles", roles);

        Integer jwtExpiration = 604800000; // 7 days
        return createToken(claims, user.getUsername(), jwtExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token has expired: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("JWT parsing error: {}", e.getMessage());
            throw e;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object id = claims.get("id");
            if (id instanceof Integer) {
                return ((Integer) id).longValue();
            }
            if (id instanceof Long) {
                return (Long) id;
            }
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> extractRoles(String token) {
        return extractClaim(token, claims -> (List<Map<String, Object>>) claims.get("roles"));
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            logger.error("Error checking token expiration: ", e);
            return true;
        }
    }

    // Overloaded method for validate endpoint - without UserDetails
    public Boolean isTokenValid(String token) {
        try {
            String tokenType = extractTokenType(token);
            return !isTokenExpired(token) && "access_token".equals(tokenType);
        } catch (Exception e) {
            logger.error("Token validation failed: ", e);
            return false;
        }
    }

    // Original method with UserDetails
    public Boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            final String tokenType = extractTokenType(token);
            return (username.equals(userDetails.getUsername()))
                    && !isTokenExpired(token)
                    && "access_token".equals(tokenType);
        } catch (Exception e) {
            logger.error("Token validation with user details failed: ", e);
            return false;
        }
    }

    // Get remaining time until token expires (in milliseconds)
    public long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            logger.error("Error getting remaining time: ", e);
            return 0;
        }
    }

    // Check if token will expire within specified minutes
    public boolean isTokenExpiringWithin(String token, long minutes) {
        try {
            long remainingTime = getRemainingTime(token);
            return remainingTime <= (minutes * 60 * 1000);
        } catch (Exception e) {
            return true;
        }
    }
}