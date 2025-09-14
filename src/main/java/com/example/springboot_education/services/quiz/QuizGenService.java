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
                        + " Trả JSON đúng schema, không thêm text ngoài JSON."
                        + " Lưu ý: options là object gồm {id, optionLabel, optionText}; id có thể null; optionLabel ∈ {A,B,C,D}.";

        // --- NEW: schema cho OptionDTO ---
        Schema optionSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "id", Schema.builder().type(Type.Known.INTEGER).build(), // optional
                        "optionLabel", Schema.builder().type(Type.Known.STRING)
                                .enum_(List.of("A","B","C","D")).build(),
                        "optionText", Schema.builder().type(Type.Known.STRING).build()
                ))
                .required(List.of("optionLabel","optionText")) // id không bắt buộc
                .propertyOrdering(List.of("id","optionLabel","optionText"))
                .build();

        // --- UPDATE: schema cho question, options = array<object> ---
        Schema questionSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "questionText", Schema.builder().type(Type.Known.STRING).build(),
                        "options", Schema.builder().type(Type.Known.ARRAY)
                                .minItems(2L).maxItems(4L) // TRUE_FALSE chỉ cần 2, MCQ thì 4
                                .items(optionSchema)
                                .build(),
                        "correctIndex", Schema.builder().type(Type.Known.INTEGER)
                                .minimum(0.0).maximum(3.0).build(),
                        "explanation", Schema.builder().type(Type.Known.STRING).build(),
                        "topic", Schema.builder().type(Type.Known.STRING).build(),
                        "difficulty", Schema.builder().type(Type.Known.STRING)
                                .enum_(List.of("easy","medium","hard")).build(),
                        "questionType", Schema.builder().type(Type.Known.STRING)
                                .enum_(List.of("ONE_CHOICE","MULTI_CHOICE","TRUE_FALSE","FILL_BLANK")).build()
                ))
                .required(List.of("questionText","options","correctIndex","questionType"))
                .propertyOrdering(List.of("questionText","options","correctIndex","explanation","topic","difficulty","questionType"))
                .build();

        Schema quizSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "quizTitle", Schema.builder().type(Type.Known.STRING).build(),
                        "questions", Schema.builder().type(Type.Known.ARRAY).items(questionSchema).build()
                ))
                .required(List.of("quizTitle","questions"))
                .propertyOrdering(List.of("quizTitle","questions"))
                .build();

        float temperature = 0.2f, topK = 40f, topP = 0.9f;
        if (settings != null && settings.getAiLevel() != null) {
            switch (settings.getAiLevel().toLowerCase()) {
                case "strict" -> { temperature = 0.1f; topK = 30f; topP = 0.8f; }
                case "creative" -> { temperature = 0.6f; topK = 50f; topP = 0.95f; }
                default -> {}
            }
        }

        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(
                        Content.builder()
                                .role("system")
                                .parts(List.of(Part.builder().text(systemInstruction).build()))
                                .build()
                )
                .responseMimeType("application/json")
                .responseSchema(quizSchema)
                .temperature(temperature)
                .topK(topK)
                .topP(topP)
                .build();

        String prompt = """
        [YÊU CẦU TỪ UI]
        %s
        Số câu cần tạo: %d.

        [VĂN BẢN TỪ FILE]
        %s

        [ĐỊNH DẠNG OPTIONS]
        Mỗi options[i] là object: { "id": null hoặc số, "optionLabel": "A|B|C|D", "optionText": "..."}.
        """.formatted(effectivePrompt, numQuestions, extractedText);

        GenerateContentResponse resp = client.models.generateContent(model, prompt, config);
        return resp.text();
    }
}
