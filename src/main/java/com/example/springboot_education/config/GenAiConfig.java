package com.example.springboot_education.config;

import com.google.genai.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenAiConfig {

    @Value("${GOOGLE_API_KEY}")
    private String apiKey;

    @Bean
    public Client genAiClient() {
        return new Client.Builder()
                .apiKey(apiKey) // truyền key vào đây
                .build();
    }
}