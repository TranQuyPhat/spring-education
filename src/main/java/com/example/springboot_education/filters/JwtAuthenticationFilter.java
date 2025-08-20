package com.example.springboot_education.filters;

import com.example.springboot_education.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Set COOP/COEP header cho Google login popup
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin-allow-popups");
        response.setHeader("Cross-Origin-Embedder-Policy", "unsafe-none");

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                username = this.jwtService.extractUsername(jwt);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    handleJwtException(response, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED.value(), request);
                    return;
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {
            handleJwtException(response, "JWT token has expired", HttpStatus.UNAUTHORIZED.value(), request);
        } catch (JwtException ex) {
            handleJwtException(response, "Invalid JWT token", HttpStatus.UNAUTHORIZED.value(), request);
        } catch (Exception ex) {
            handleJwtException(response, "Authentication error: " + ex.getMessage(), HttpStatus.UNAUTHORIZED.value(), request);
        }
    }

    private void handleJwtException(HttpServletResponse response, String message, int status, HttpServletRequest request) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", status);
        errorResponse.put("error", status == 401 ? "Unauthorized" : "Bad Request");
        errorResponse.put("messages", List.of(message));
        errorResponse.put("path", request.getServletPath());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
