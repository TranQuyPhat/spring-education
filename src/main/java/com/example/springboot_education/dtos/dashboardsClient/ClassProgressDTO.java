package com.example.springboot_education.dtos.dashboardsClient;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@Builder
@NoArgsConstructor  
@AllArgsConstructor
public class ClassProgressDTO {
    private String className;
    private Long completed;
    private Long total;
}
