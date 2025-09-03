package com.example.springboot_education.dtos.joinrequest;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateJoinRequestRequest {
    @NotNull(message = "Student ID is required")
    private Integer studentId;
}