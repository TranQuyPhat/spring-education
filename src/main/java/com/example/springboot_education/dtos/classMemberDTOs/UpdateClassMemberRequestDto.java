package com.example.springboot_education.dtos.classMemberDTOs;

import java.sql.Timestamp;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClassMemberRequestDto {
  @NotNull(message = "student_id is required")
  private Integer student_id;

  @NotNull(message = "class_id is required")
  private Integer class_id;

  private Timestamp joined_at;

}
