package com.example.springboot_education.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.springboot_education.entities.Role;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.UsersJpaRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersJpaRepository userRepository;

    public CustomUserDetailsService(UsersJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("🔥 loadUserByUsername: " + username);
        Users user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
       user.getUserRoles().forEach(userRole -> {
    Role role = userRole.getRole();
            // Nếu dùng @PreAuthorize("hasAuthority('Administrators')") thì
            authorities.add(new SimpleGrantedAuthority(role.getName()));

            // Nếu dùng @PreAuthorize("hasRole('Administrators')") thì authorities.add(new
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        });

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}