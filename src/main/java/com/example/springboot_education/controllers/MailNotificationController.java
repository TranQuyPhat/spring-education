package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.mail.ClassChangeRequestDto;
import com.example.springboot_education.services.mail.EmailService;
import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailNotificationController {

    private final EmailService emailService;

    // Thông báo nghỉ / học bù
    @PostMapping("/class-change")
    public ResponseEntity<String> sendClassChange(@RequestBody ClassChangeRequestDto request) {
        String html = emailService.buildClassChangeTemplate(
                request.getFullName(),
                request.getClassName(),
                request.getType(),
                request.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                request.getNote());
        String subject = "Thông báo " + (request.getType().equalsIgnoreCase("cancel") ? "Nghỉ học" : "Học bù");
        emailService.sendEmail(request.getTo(), subject, html);
        return ResponseEntity.ok("Class change mail sent to " + request.getTo());
    }
}
