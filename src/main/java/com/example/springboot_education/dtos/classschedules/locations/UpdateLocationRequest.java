package com.example.springboot_education.dtos.classschedules.locations;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLocationRequest {
    @NotNull(message = "Room Name is required")
    private String roomName;
    private String description;
}
