package com.example.springboot_education.dtos.quiz;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionResponseDTO {
    private Integer id;
    private String label;
    private String text;
}
