package com.example.springboot_education.dtos.classMemberDTOs;

import java.sql.Timestamp;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassMemberResponseDto {
    private Long class_id;
    private Long student_id;
    private Timestamp joined_at;
}
