package com.example.springboot_education.services;

import com.example.springboot_education.dtos.AuthDTOs.GoogleLoginRequestDto;
import com.example.springboot_education.dtos.AuthDTOs.GoogleLoginWithCredentialRequestDto;
import com.example.springboot_education.dtos.AuthDTOs.LoginRequestDto;
import com.example.springboot_education.dtos.AuthDTOs.LoginResponseDto;
import com.example.springboot_education.dtos.AuthDTOs.RegisterRequestDto;
import com.example.springboot_education.dtos.AuthDTOs.RegisterResponseDto;
import com.example.springboot_education.entities.Role;
import com.example.springboot_education.entities.UserRole;
import com.example.springboot_education.entities.UserRoleId;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.repositories.RoleJpaRepository;
import com.example.springboot_education.repositories.UserRoleRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final JwtService jwtService;
    private final UsersJpaRepository userJpaRepository;
    private final RoleJpaRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Đăng ký
    public RegisterResponseDto register(RegisterRequestDto request) {
        if (userJpaRepository.existsByEmail(request.getEmail())) {
            throw new HttpException("Email already exists", HttpStatus.CONFLICT);
        }

        if (userJpaRepository.existsByUsername(request.getUsername())) {
            throw new HttpException("Username already exists", HttpStatus.CONFLICT);
        }

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new HttpException("Role not found", HttpStatus.BAD_REQUEST));

        Users newUser = new Users();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setFullName(request.getFullName());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        Users savedUser = userJpaRepository.save(newUser);

        UserRole userRole = new UserRole();
        userRole.setId(new UserRoleId(savedUser.getId(), role.getId()));
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        String token = jwtService.generateAccessToken(savedUser);

        return RegisterResponseDto.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .accessToken(token)
                .build();
    }

    // ✅ Đăng nhập
    public LoginResponseDto login(LoginRequestDto request) {
        Users user = null;

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user = userJpaRepository.findByEmailWithRoles(request.getEmail())
                    .orElseThrow(() -> new HttpException("Invalid email or password", HttpStatus.UNAUTHORIZED));
        } else if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            user = userJpaRepository.findByUsernameWithRoles(request.getUsername())
                    .orElseThrow(() -> new HttpException("Invalid username or password", HttpStatus.UNAUTHORIZED));
        } else {
            throw new HttpException("Username or Email is required", HttpStatus.BAD_REQUEST);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new HttpException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtService.generateAccessToken(user);

        List<String> roles = user.getUserRoles()
    .stream()
    .map(ur -> ur.getRole().getName())
    .collect(Collectors.toList());


        return LoginResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .accessToken(accessToken)
                  .roles(roles) 
                .build();
    }

    public LoginResponseDto googleLogin(GoogleLoginRequestDto request) {
        Optional<Users> user = userJpaRepository.findByEmail(request.getEmail());
        Users newUser;

        if (user.isEmpty()) {
            newUser = new Users();
            newUser.setUsername(request.getEmail());
            newUser.setPassword(""); // Google login không cần mật khẩu
                newUser.setEmail(request.getEmail()); 

            // Lưu user để lấy ID
            newUser = userJpaRepository.save(newUser);

            // Gán role ID = 3
            Role role = new Role();
            role.setId(3);

            UserRoleId userRoleId = new UserRoleId(newUser.getId(), role.getId());
            UserRole userRole = new UserRole();
            userRole.setId(userRoleId);
            userRole.setUser(newUser);
            userRole.setRole(role);
            userRole.setEnabled(true);

            // Gán UserRole vào user
            newUser.setUserRoles(List.of(userRole));

            // Lưu lại user kèm roles
            userJpaRepository.save(newUser);
        } else {
            newUser = user.get();
        }

        String accessToken = jwtService.generateAccessToken(newUser);

        return LoginResponseDto.builder()
                .userId(newUser.getId())
                .username(newUser.getUsername())
                .email(newUser.getEmail())
                .accessToken(accessToken)
                .build();

    }

    public LoginResponseDto googleLoginWithCredential(GoogleLoginWithCredentialRequestDto request) {
        // Call google API to verify the token
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getCredential();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> payload = response.getBody();

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new HttpException("Invalid Google token", HttpStatus.UNAUTHORIZED);
        }

        // Parse the response to get the email
        String email;
        if (payload != null && payload.containsKey("email")) {
            email = payload.get("email").toString();
        } else {
            throw new HttpException("Email not found in token", HttpStatus.UNAUTHORIZED);
        }

        // Nên kiểm tra aud = Client ID của ứng dụng để đảm bảo token hợp lệ ( // Có thể
        // code sau ...)
        // String aud = payload.get("aud").toString();
        // if (!aud.equals("YOUR_CLIENT_ID")) {
        // throw new HttpException("Invalid Google token audience",
        // HttpStatus.UNAUTHORIZED);
        // }

        // Kiểm tra thêm: exp so với thời gian hiện tại để đảm bảo token chưa hết hạn.
        String iss = payload.get("iss").toString();
        if (!iss.equals("https://accounts.google.com") && !iss.equals("accounts.google.com")) {
            throw new HttpException("Invalid Google token issuer", HttpStatus.UNAUTHORIZED);
        }

        // Kiểm tra thêm: exp so với thời gian hiện tại để đảm bảo token chưa hết hạn.
        long exp = Long.parseLong(payload.get("exp").toString());
        if (exp < System.currentTimeMillis() / 1000) {
            throw new HttpException("Google token has expired", HttpStatus.UNAUTHORIZED);
        }

        // Find the user by email
        // If not found, create a new user with the email.
        Optional<Users> user = this.userJpaRepository.findByEmail(email);

        // Create new user if not found
        Users newUser = user.orElseGet(() -> {
            Users u = new Users();
            u.setUsername(email);
            u.setPassword("");
            u.setEmail(email); 

            // Lưu user trước để có ID
            Users savedUser = userJpaRepository.save(u);

            // Gán role ID = 3
            Role role = new Role();
            role.setId(3);

            UserRoleId userRoleId = new UserRoleId(savedUser.getId(), role.getId());
            UserRole userRole = new UserRole();
            userRole.setId(userRoleId);
            userRole.setUser(savedUser);
            userRole.setRole(role);
            userRole.setEnabled(true);

            // Gán UserRole vào user
            savedUser.setUserRoles(List.of(userRole));

            return userJpaRepository.save(savedUser);
        });

        // Generate a new access token (with full data + roles)
        String accessToken = jwtService.generateAccessToken(newUser);

        return LoginResponseDto.builder()
                .userId(newUser.getId())
                .username(newUser.getUsername())
                .accessToken(accessToken)
                .build();
    }

}
