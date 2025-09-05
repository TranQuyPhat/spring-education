package com.example.springboot_education.dtos.classschedules;


import lombok.Data;

@Data
public class SessionStatusUpdateDTO {
    private String status; // CANCELLED, COMPLETED,...
}