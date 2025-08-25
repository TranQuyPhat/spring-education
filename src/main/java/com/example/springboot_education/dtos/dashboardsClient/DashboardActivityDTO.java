package com.example.springboot_education.dtos.dashboardsClient;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActivityDTO {
    private String message;     
    private String type;         
    private String className;    
    private String time;   
  private Instant sortTime; 
}
