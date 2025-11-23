package com.example.springboot_education.services.assignment;
import org.springframework.stereotype.Service;

import com.example.springboot_education.dtos.assignmentDTOs.NotificationAssignmentDTO;


import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceAssignment {
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyClass(Integer classId, NotificationAssignmentDTO payload) {
    try {
        String destination = "/topic/class/" + classId + "/assignments";
        messagingTemplate.convertAndSend(destination, payload);
        System.out.println("Notification sent to class topic: " + destination);
    } catch (Exception e) {
        System.err.println("Error sending class notification: " + e.getMessage());
        e.printStackTrace();
    }
}
}
