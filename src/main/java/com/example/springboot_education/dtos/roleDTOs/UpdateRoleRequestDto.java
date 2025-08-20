package com.example.springboot_education.dtos.roleDTOs;
// import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequestDto {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

      public boolean hasAnyField() {
        return name != null; 
    }
}