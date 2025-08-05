package com.example.springboot_education.services;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.springboot_education.entities.Role;
import com.example.springboot_education.entities.Users;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class JwtService {

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
        claims.put("type", "access_token"); // Token type identifier

        // Add user roles to access token
        /*
         * Trường hợp KHÔNG nên đưa roles vào JWT:
         * Bảo mật cao: Roles có thể thay đổi thường xuyên, nếu để trong JWT thì phải
         * chờ token hết hạn mới cập nhật được.
         * Roles phức tạp: Nếu roles có nhiều thông tin chi tiết, JWT sẽ trở nên lớn.
         * Quản lý tập trung: Muốn kiểm soát quyền truy cập real-time từ database.
         * Trường hợp NÊN đưa roles vào JWT:
         * Performance: Tránh query database mỗi request để lấy roles.
         * Stateless: Hoàn toàn không phụ thuộc vào database cho việc xác thực.
         * Microservices: Các service khác có thể đọc roles từ JWT mà không cần gọi user
         * service.
         */
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

        Integer jwtExpiration = 86400000;
        return createToken(claims, user.getUsername(), jwtExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
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

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        final String tokenType = extractTokenType(token);
        return (username.equals(userDetails.getUsername()))
                && !isTokenExpired(token)
                && "access_token".equals(tokenType); // Only access tokens for authentication
    }
}
