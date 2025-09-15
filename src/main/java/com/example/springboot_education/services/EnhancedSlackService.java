package com.example.springboot_education.services;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.conversations.ConversationsCreateRequest;
import com.slack.api.methods.request.conversations.ConversationsSetTopicRequest;
import com.slack.api.methods.response.conversations.ConversationsCreateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EnhancedSlackService {

    @Autowired
    private MethodsClient slack;

    public SlackChannelInfo createPrivateChannel(String channelName, String description) {
        try {
            ConversationsCreateRequest request = ConversationsCreateRequest.builder()
                    .name(channelName)
                    .isPrivate(true)
                    .build();

            ConversationsCreateResponse response = slack.conversationsCreate(request);

            if (response.isOk()) {
                String channelId = response.getChannel().getId();

                // Set topic
                if (description != null && !description.isEmpty()) {
                    slack.conversationsSetTopic(ConversationsSetTopicRequest.builder()
                            .channel(channelId)
                            .topic(description)
                            .build());
                }

                // Create shareable link
                String inviteLink = createShareableLink(channelId);

                return new SlackChannelInfo(channelId, inviteLink, channelName);

            } else {
                log.error("Failed to create Slack channel: {}", response.getError());
                return null;
            }

        } catch (Exception e) {
            log.error("Error creating Slack channel", e);
            return null;
        }
    }

    private String createShareableLink(String channelId) {
        // Tạo một deep link để join channel
        return String.format("https://slack.com/app_redirect?channel=%s", channelId);
    }

    // Data class để return channel info
    public static class SlackChannelInfo {
        private final String channelId;
        private final String inviteLink;
        private final String channelName;

        public SlackChannelInfo(String channelId, String inviteLink, String channelName) {
            this.channelId = channelId;
            this.inviteLink = inviteLink;
            this.channelName = channelName;
        }

        // Getters
        public String getChannelId() { return channelId; }
        public String getInviteLink() { return inviteLink; }
        public String getChannelName() { return channelName; }
    }
}