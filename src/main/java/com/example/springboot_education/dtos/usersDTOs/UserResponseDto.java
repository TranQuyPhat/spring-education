package com.example.springboot_education.dtos.usersDTOs;
import java.sql.Timestamp;
import java.util.List;

import com.example.springboot_education.dtos.roleDTOs.RoleResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Integer id;
    private String username;
    
    @JsonProperty("full_name") 
    private String fullName;
    
    @JsonProperty("image_url") 
    private String imageUrl;
    
    private String email;
    private List<RoleResponseDto> roles;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    
}