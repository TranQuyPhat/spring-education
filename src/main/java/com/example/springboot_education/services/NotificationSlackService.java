package com.example.springboot_education.services;

import com.example.springboot_education.repositories.ClassRepository;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationSlackService {

    @Autowired
    private MethodsClient slack;  // Slack Java SDK

    @Autowired
    private ClassRepository classRepository;

    public void sendSlackNotification(Integer classId,
                                      ClassEventType eventType,
                                      Map<String, Object> data) {
        // 1. Lấy Slack channel ID của lớp
        String channelId = classRepository.findSlackChannelIdById(classId);
        if (channelId == null) {
            throw new RuntimeException("Class " + classId + " chưa có Slack channel ID");
        }
        // 2. Xây dựng message theo eventType
        String message = buildMessage(eventType, data);

        // 3. Gửi lên Slack
        ChatPostMessageRequest req = ChatPostMessageRequest.builder()
                .channel(channelId)
                .text(message)
                .build();

        try {
            ChatPostMessageResponse resp = slack.chatPostMessage(req);
            if (!resp.isOk()) {
                System.err.println("Slack send error: " + resp.getError());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildMessage(ClassEventType type, Map<String, Object> data) {
        switch (type) {
            case ASSIGNMENT_CREATED:
                return "Giáo viên " + data.get("teacher") +
                        " vừa giao bài tập: *" + data.get("title") + "*";
            case QUIZ_CREATED:
                return "Quiz mới: *" + data.get("quizTitle") + "* - Hãy vào làm nhé!";
            case QUIZ_SUBMITTED:
                return "Sinh viên " + data.get("student") +
                        " đã nộp quiz: " + data.get("quizTitle");
            case MAKEUP_CLASS:
                return "Lịch học bù: " + data.get("date") + " - " + data.get("note");
            case CLASS_CANCELLED:
                return "Buổi học ngày " + data.get("date") + " đã bị huỷ.";
            default:
                return "Có sự kiện mới trong lớp!";
        }
    }
    public enum ClassEventType {
        ASSIGNMENT_CREATED,
        QUIZ_CREATED,
        QUIZ_SUBMITTED,
        MAKEUP_CLASS,
        CLASS_CANCELLED
        // ... có thể mở rộng
    }
}
