package com.example.springboot_education.dtos.joinrequest;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalResponseDTO {
    private Integer requestId;
    private boolean approved;
    private String message;
}