package com.example.springboot_education.config;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        System.out.println("🔗 New WebSocket connection: " + sessionId);
        System.out.println("User: " + headerAccessor.getUser());
        System.out.println("Principal: " + event.getUser());
        
        if (event.getUser() != null) {
            System.out.println("✅ User Principal: " + event.getUser().getName());
        } else {
            System.out.println("❌ No user principal found");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        System.out.println("❌ WebSocket connection closed: " + sessionId);
        if (event.getUser() != null) {
            System.out.println("Disconnected user: " + event.getUser().getName());
        }
    }

    @EventListener
    public void handleSubscriptionEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        System.out.println("📡 New subscription:");
        System.out.println("  Session: " + sessionId);
        System.out.println("  Destination: " + destination);
        System.out.println("  User: " + (event.getUser() != null ? event.getUser().getName() : "anonymous"));
    }
}
