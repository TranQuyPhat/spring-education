package com.example.springboot_education.dtos.roleDTOs;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public RoleResponseDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}