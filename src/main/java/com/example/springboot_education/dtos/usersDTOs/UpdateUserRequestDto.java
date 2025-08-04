package com.example.springboot_education.dtos.usersDTOs;

import com.example.springboot_education.entities.Role;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDto {
     private String username;

     private String full_name;

    @Email(message = "Email is invalid")
    private String email;

    private String password;

    private Role role;
}
