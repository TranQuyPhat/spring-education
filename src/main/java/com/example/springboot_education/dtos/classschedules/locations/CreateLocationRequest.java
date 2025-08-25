package com.example.springboot_education.dtos.classschedules.locations;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLocationRequest {
    private String roomName;
    private String description;
}