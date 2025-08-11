package com.example.springboot_education.dtos.classDTOs;

import java.time.Instant;

import lombok.Data;

@Data
public class ClassMemberDTO {
    private Integer id;
    private String username;
    private String fullName;
    private String email;
    private Instant joinedAt;
}
