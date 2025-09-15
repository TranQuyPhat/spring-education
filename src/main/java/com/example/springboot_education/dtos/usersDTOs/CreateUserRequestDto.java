package com.example.springboot_education.dtos.usersDTOs;

import java.sql.Timestamp;
import java.util.List;

// import com.example.springboot_education.entities.Users;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor  
public class CreateUserRequestDto {
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Full name is required")
      @JsonProperty("full_name") 
    private String fullName;  
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;
    
      @JsonProperty("avatar") 
     private String avatarBase64;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,100}$", message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
    private String password;

    @NotEmpty(message = "At least one role is required")
    private List<Integer> roles;

    private  Timestamp createdAt;
    private  Timestamp updatedAt;
      
}