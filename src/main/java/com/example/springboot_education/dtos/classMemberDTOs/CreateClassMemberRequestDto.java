package com.example.springboot_education.dtos.classMemberDTOs;

import java.sql.Timestamp;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateClassMemberRequestDto {
      @NotNull(message = "student_id is required")
    private Long student_id;

    @NotNull(message = "class_id is required")
    private Long class_id;
    private Timestamp joined_at; 
}