package com.example.springboot_education.services;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.auth.AuthTestRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.conversations.*;
import com.slack.api.methods.response.auth.AuthTestResponse;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsCreateResponse;
import com.slack.api.methods.response.conversations.ConversationsInfoResponse;
import com.slack.api.methods.response.conversations.ConversationsInviteResponse;
import com.slack.api.methods.response.conversations.ConversationsSetTopicResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

    @Service
    @Slf4j
    public class SlackService {

        @Autowired
        private MethodsClient slack;
        public SlackChannelResult createPublicChannel(String channelName, String description) {
            try {
                String validName = validateAndFormatChannelName(channelName);
                ConversationsCreateRequest request = ConversationsCreateRequest.builder()
                        .name(validName)
                        .isPrivate(false) // <== PUBLIC
                        .build();

                ConversationsCreateResponse response = slack.conversationsCreate(request);

                if (response.isOk()) {
                    String channelId = response.getChannel().getId();
                    if (description != null && !description.isEmpty()) {
                        setChannelTopic(channelId, description);
                    }
                    return new SlackChannelResult(true,
                            channelId,
                            String.format("https://spring-education.slack.com/archives/%s",channelId),
                            response.getChannel().getName(),
                            null);
                } else {
                    return new SlackChannelResult(false, null, null, null, response.getError());
                }
            } catch (Exception e) {
                log.error("Error creating public Slack channel", e);
                return new SlackChannelResult(false, null, null, null, e.getMessage());
            }
        }

        public String lookupUserIdByEmail(String email) {
            try {
                var resp = slack.usersLookupByEmail(r -> r.email(email));
                return resp.isOk() ? resp.getUser().getId() : null;
            } catch (Exception e) {
                log.error("Error lookup Slack user by email", e);
                return null;
            }
        }

        public SlackChannelResult createPrivateChannel(String channelName, String description) {
            try {
                log.info("Attempting to create Slack channel: {}", channelName);

                // Validate channel name format
                String validChannelName = validateAndFormatChannelName(channelName);
                log.info("Formatted channel name: {}", validChannelName);

                ConversationsCreateRequest request = ConversationsCreateRequest.builder()
                        .name(validChannelName)
                        .isPrivate(true)
                        .build();

                ConversationsCreateResponse response = slack.conversationsCreate(request);

                log.info("Slack API response - OK: {}, Error: {}", response.isOk(), response.getError());

                if (response.isOk()) {
                    String channelId = response.getChannel().getId();
                    String channelName_actual = response.getChannel().getName();

                    log.info("Successfully created channel - ID: {}, Name: {}", channelId, channelName_actual);

                    // Set topic if provided
                    if (description != null && !description.isEmpty()) {
                        setChannelTopic(channelId, description);
                    }

                    // Verify channel exists
                    if (verifyChannelExists(channelId)) {
                        String inviteLink = generateCorrectInviteLink(channelId);
                        log.info("Generated invite link: {}", inviteLink);

                        return new SlackChannelResult(true, channelId, inviteLink, channelName_actual, null);
                    } else {
                        log.error("Channel was created but verification failed");
                        return new SlackChannelResult(false, null, null, null, "Channel verification failed");
                    }

                } else {
                    log.error("Failed to create Slack channel: {}", response.getError());
                    return new SlackChannelResult(false, null, null, null, response.getError());
                }

            } catch (Exception e) {
                log.error("Exception creating Slack channel", e);
                return new SlackChannelResult(false, null, null, null, e.getMessage());
            }
        }
        private String generateCorrectInviteLink(String channelId) {
            // Try multiple formats to find the working one
            // Format 1: Direct slack app link
            return String.format("slack://channel?team=%s&id=%s", getWorkspaceId(), channelId);
        }
        private String getWorkspaceId() {
            try {
                AuthTestResponse authResponse = slack.authTest(AuthTestRequest.builder().build());
                if (authResponse.isOk()) {
                    return authResponse.getTeamId();
                }
            } catch (Exception e) {
                log.error("Error getting workspace ID", e);
            }
            return ""; // fallback
        }
        private boolean verifyChannelExists(String channelId) {
            try {
                ConversationsInfoRequest request = ConversationsInfoRequest.builder()
                        .channel(channelId)
                        .build();

                ConversationsInfoResponse response = slack.conversationsInfo(request);
                boolean exists = response.isOk() && response.getChannel() != null;

                log.info("Channel verification - exists: {}, error: {}", exists, response.getError());
                return exists;

            } catch (Exception e) {
                log.error("Error verifying channel exists", e);
                return false;
            }
        }private void setChannelTopic(String channelId, String topic) {
            try {
                ConversationsSetTopicRequest request = ConversationsSetTopicRequest.builder()
                        .channel(channelId)
                        .topic(topic)
                        .build();

                ConversationsSetTopicResponse response = slack.conversationsSetTopic(request);
                log.info("Set topic result: {}", response.isOk());

            } catch (Exception e) {
                log.error("Error setting channel topic", e);
            }
        }
        private String generateInviteLink(String channelId) {
            try {
                // Tạo invite link cho channel
                ConversationsInviteSharedRequest request = ConversationsInviteSharedRequest.builder()
                        .channel(channelId)
                        .build();

                // Slack không có direct API để tạo invite link,
                // nên chúng ta sẽ tạo một deep link
                return String.format("https://slack.com/app_redirect?channel=%s", channelId);

            } catch (Exception e) {
                log.error("Error generating invite link", e);
                return String.format("https://slack.com/app_redirect?channel=%s", channelId);
            }
        }

        public boolean inviteUserToChannel(String channelId, String userId) {
            try {
                ConversationsInviteRequest request = ConversationsInviteRequest.builder()
                        .channel(channelId)
                        .users(Arrays.asList(userId))
                        .build();

                ConversationsInviteResponse response = slack.conversationsInvite(request);
                return response.isOk();

            } catch (Exception e) {
                log.error("Error inviting user to channel", e);
                return false;
            }
        }

        public String sendWelcomeMessage(String channelId, String className, String teacherName) {
            try {
                String message = String.format(
                        "🎉 Chào mừng đến với lớp học *%s*!\n" +
                                "👨‍🏫 Giáo viên: %s\n" +
                                "📚 Hãy cùng nhau học tập hiệu quả!",
                        className, teacherName
                );

                ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                        .channel(channelId)
                        .text(message)
                        .build();

                ChatPostMessageResponse response = slack.chatPostMessage(request);

                if (response.isOk()) {
                    return response.getTs();
                }

            } catch (Exception e) {
                log.error("Error sending welcome message", e);
            }
            return null;
        }
        private String validateAndFormatChannelName(String channelName) {
            // Slack channel name rules:
            // - lowercase only
            // - no spaces (replace with -)
            // - only letters, numbers, hyphens, underscores
            // - max 21 characters
            // - cannot start or end with hyphen

            String formatted = channelName.toLowerCase()
                    .replaceAll("[^a-z0-9\\-_]", "-")
                    .replaceAll("-+", "-")  // Remove consecutive hyphens
                    .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens

            if (formatted.length() > 21) {
                formatted = formatted.substring(0, 21);
            }

            // Ensure it doesn't end with hyphen after truncation
            formatted = formatted.replaceAll("-$", "");

            return formatted;
        }

        public static class SlackChannelResult {
            private final boolean success;
            private final String channelId;
            private final String inviteLink;
            private final String channelName;
            private final String error;

            public SlackChannelResult(boolean success, String channelId, String inviteLink, String channelName, String error) {
                this.success = success;
                this.channelId = channelId;
                this.inviteLink = inviteLink;
                this.channelName = channelName;
                this.error = error;
            }

            // Getters
            public boolean isSuccess() { return success; }
            public String getChannelId() { return channelId; }
            public String getInviteLink() { return inviteLink; }
            public String getChannelName() { return channelName; }
            public String getError() { return error; }
        }

    }
