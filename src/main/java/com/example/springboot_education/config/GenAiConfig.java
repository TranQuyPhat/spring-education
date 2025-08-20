package com.example.springboot_education.config;

import com.google.genai.Client;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenAiConfig {

    Dotenv dotenv = Dotenv.load();
    String apiKey = dotenv.get("GOOGLE_API_KEY_ENV");

    @Bean
    public Client genAiClient() {
        return new Client.Builder()
                .apiKey(apiKey)
                .build();
    }
}