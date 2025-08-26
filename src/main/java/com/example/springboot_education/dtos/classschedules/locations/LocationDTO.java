package com.example.springboot_education.dtos.classschedules.locations;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDTO {
    private Integer id;
    private String roomName;
    private String description;
}
