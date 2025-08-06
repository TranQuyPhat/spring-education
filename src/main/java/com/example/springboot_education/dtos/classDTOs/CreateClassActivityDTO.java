// CreateClassActivityDTO.java
package com.example.springboot_education.dtos.classDTOs;


import lombok.Data;

@Data
public class CreateClassActivityDTO {
    private String actionType;
    private Integer targetId;
    private String targetTable;
    private String description;
    // private Timestamp createdAt;
    private Integer classId;
    private Integer userId;
}