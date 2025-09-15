package com.example.springboot_education.config;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfig {

    @Value("${SLACK_BOT_TOKEN}")
    private String botToken;

    @Bean
    public Slack slackClient() {
        return Slack.getInstance();
    }

    @Bean
    public MethodsClient slackMethods() {
        return slackClient().methods(botToken);
    }
}
