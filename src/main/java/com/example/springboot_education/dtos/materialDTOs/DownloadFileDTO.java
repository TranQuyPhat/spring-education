package com.example.springboot_education.dtos.materialDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class DownloadFileDTO {
    private Resource resource;
    private String fileType;
    private String fileName;
}