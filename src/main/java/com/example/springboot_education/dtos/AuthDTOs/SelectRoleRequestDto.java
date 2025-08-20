package com.example.springboot_education.dtos.AuthDTOs;

public class SelectRoleRequestDto {
    private Integer userId;
    private String role; 

    // getter và setter
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}