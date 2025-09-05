package com.example.springboot_education.controllers.quiz;

import com.example.springboot_education.dtos.quiz.QuestionResponse;
import com.example.springboot_education.services.quiz.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private DocumentService fileProcessingService;

    @PostMapping("/extract-questions")
    public ResponseEntity<QuestionResponse> extractQuestions(
            @RequestParam("file") MultipartFile file) {

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Check file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().build();
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !isValidFileType(fileName)) {
                return ResponseEntity.badRequest().build();
            }

            QuestionResponse response = fileProcessingService.processFile(file);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isValidFileType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return extension.equals("pdf") || extension.equals("txt");
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("PDF File Processing Service is running!");
    }
}