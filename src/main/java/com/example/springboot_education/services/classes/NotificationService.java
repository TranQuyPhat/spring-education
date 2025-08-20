package com.example.springboot_education.services.classes;



import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.springboot_education.dtos.joinrequest.ApprovalResponseDTO;
import com.example.springboot_education.dtos.joinrequest.JoinRequestDTO;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyTeacher(Integer teacherId, JoinRequestDTO payload) {
        System.out.println("=== NOTIFICATION DEBUG ===");
        System.out.println("Teacher ID: " + teacherId);
        System.out.println("Payload: " + payload);
        
        try {
            // Cách 1: Dùng convertAndSendToUser (Spring tự động thêm /user prefix)
            // messagingTemplate.convertAndSendToUser(
            //     String.valueOf(teacherId), 
            //     "/queue/join-requests", 
            //     payload
            // );
            // System.out.println("✅ Message sent via convertAndSendToUser");
            
            // Cách 2: Thử gửi trực tiếp đến destination đầy đủ
            String fullDestination = "/user/" + teacherId + "/queue/join-requests";
            messagingTemplate.convertAndSend(fullDestination, payload);
            System.out.println("✅ Message sent to full destination: " + fullDestination);
            
            // Cách 3: Thử với session ID (nếu có)
            // messagingTemplate.convertAndSendToUser(sessionId, "/queue/join-requests", payload);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending notification: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== END NOTIFICATION DEBUG ===");
    }

    public void notifyStudent(Integer studentId, ApprovalResponseDTO payload) {
        try {
            messagingTemplate.convertAndSendToUser(
                String.valueOf(studentId), 
                "/queue/join-requests-response", 
                payload
            );
            System.out.println("✅ Student notification sent successfully");
        } catch (Exception e) {
            System.err.println("❌ Error sending student notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}