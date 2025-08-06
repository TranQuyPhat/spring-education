// ClassActivityDTO.java
package com.example.springboot_education.dtos.classDTOs;

import java.time.Instant;

import lombok.Data;

// import java.sql.Timestamp;

@Data
public class ClassActivityDTO {
    private Integer id;
    private String actionType;
    private Integer targetId;
    private String targetTable;
    private String description;
    private Instant createdAt;
    private Integer classId;
    private Integer userId;
}


