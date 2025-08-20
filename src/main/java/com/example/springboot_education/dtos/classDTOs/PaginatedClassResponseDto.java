package com.example.springboot_education.dtos.classDTOs;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedClassResponseDto {
    private List<ClassResponseDTO> data;
    private int pageNumber;
    private int pageSize;
    private long totalRecords;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
} 