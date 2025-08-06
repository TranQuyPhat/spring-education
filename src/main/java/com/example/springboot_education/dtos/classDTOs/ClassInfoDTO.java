package com.example.springboot_education.dtos.classDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassInfoDTO {
    private Integer id;
    private String className;
    private String subject;
    private Integer schoolYear;
    private String semester;
}
