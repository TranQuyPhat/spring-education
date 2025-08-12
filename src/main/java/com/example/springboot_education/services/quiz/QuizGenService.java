// src/main/java/.../services/quiz/QuizGenService.java
package com.example.springboot_education.services.quiz;

import com.example.springboot_education.dtos.quiz.AiQuizSettings;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import java.util.List;
import java.util.Map;

@Service
public class QuizGenService {

    private final Client client;

    @Value("${GEMINI_MODEL:gemini-2.5-flash}")
    private String model;

    public QuizGenService(Client client) {
        this.client = client;
    }

    public String generateQuizJson(String extractedText,
                                   String effectivePrompt,
                                   int numQuestions,
                                   AiQuizSettings settings) {

        String systemInstruction =
                "Bạn là trợ giảng nghiêm túc. Chỉ dựa vào nội dung tài liệu để tạo câu hỏi trắc nghiệm."
                        + " Mỗi câu có 4 phương án (A–D) và duy nhất 1 đáp án đúng."
                        + " Bổ sung ngắn gọn: explanation, topic, difficulty (easy|medium|hard)."
                        + " Trả về JSON đúng schema, không thêm text ngoài JSON.";

        // 2) Schema
        Schema questionSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "questionText", Schema.builder().type(Type.Known.STRING).build(),
                        "options", Schema.builder().type(Type.Known.ARRAY)
                                .minItems(4L).maxItems(4L)
                                .items(Schema.builder().type(Type.Known.STRING).build())
                                .build(),
                        "correctIndex", Schema.builder().type(Type.Known.INTEGER)
                                .minimum(0.0).maximum(3.0).build(),
                        "explanation", Schema.builder().type(Type.Known.STRING).build(),
                        "topic", Schema.builder().type(Type.Known.STRING).build(),
                        "difficulty", Schema.builder().type(Type.Known.STRING)
                                .enum_(List.of("easy","medium","hard")).build()
                ))
                .required(List.of("questionText","options","correctIndex"))
                .propertyOrdering(List.of("questionText","options","correctIndex","explanation","topic","difficulty"))
                .build();

        Schema quizSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "quizTitle", Schema.builder().type(Type.Known.STRING).build(),
                        "questions", Schema.builder().type(Type.Known.ARRAY).items(questionSchema).build()
                ))
                .required(List.of("questions"))
                .propertyOrdering(List.of("quizTitle","questions"))
                .build();

        // 3) Map aiLevel -> cấu hình sinh
        float temperature = 0.2f, topK = 40f, topP = 0.9f;
        if (settings != null && settings.getAiLevel() != null) {
            switch (settings.getAiLevel().toLowerCase()) {
                case "strict" -> { temperature = 0.1f; topK = 30f; topP = 0.8f; }
                case "creative" -> { temperature = 0.6f; topK = 50f; topP = 0.95f; }
                default -> { /* balanced mặc định */ }
            }
        }

        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction( Content.builder()
                        .role("system")
                        .parts(List.of(Part.builder()
                                .text(systemInstruction)
                                .build()))
                        .build())
                .responseMimeType("application/json")
                .responseSchema(quizSchema)
                .temperature(temperature)
                .topK(topK)
                .topP(topP)
                .build();

        // 4) Prompt cuối chỉ chứa yêu cầu + dữ liệu
        String prompt = """
        [YÊU CẦU TỪ UI]
        %s
        Số câu cần tạo: %d.

        [VĂN BẢN TỪ FILE]
        %s
        """.formatted(effectivePrompt, numQuestions, extractedText);

        GenerateContentResponse resp =
                client.models.generateContent(model, prompt, config);

        return resp.text(); // JSON hợp lệ theo schema
    }
}
