package com.example.springboot_education.dtos.classDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassAggregateDTO {
    private Integer classId;
    private String className;
    private Long students;
    private Long assignments;
    private Long materials;
    private Long submissions;

}