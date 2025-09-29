package com.example.springboot_education.controllers.quiz;

import com.example.springboot_education.dtos.ApiResponse;
import com.example.springboot_education.dtos.quiz.AiQuizSettings;
import com.example.springboot_education.services.quiz.FileParseService;
import com.example.springboot_education.services.quiz.QuizGenService;
import com.example.springboot_education.untils.TextChunker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/quiz")
public class QuizGenController {

    private final FileParseService fileParseService;
    private final QuizGenService quizGenService;
    private final ObjectMapper om = new ObjectMapper();

    public QuizGenController(FileParseService fileParseService, QuizGenService quizGenService) {
        this.fileParseService = fileParseService;
        this.quizGenService = quizGenService;
    }

    @PostMapping(value = "/generate-from-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> generateFromFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart("settings") AiQuizSettings settings
    ) {
        try {
            int numQuestions = settings.getNumQuestions() != null ? settings.getNumQuestions() : 10;
            if (numQuestions < 1) numQuestions = 1;
            if (numQuestions > 100) numQuestions = 100; // guard
            String text = fileParseService.parseToPlainText(file);
            List<String> chunks = TextChunker.splitBySize(text, 12000);
            if (chunks.isEmpty()) chunks = List.of(text);
            String effectivePrompt = buildPromptFromSettings(settings);
            List<JsonNode> all = new ArrayList<>();
            String quizTitle = (settings.getQuizTitle() != null && !settings.getQuizTitle().isBlank())
                    ? settings.getQuizTitle() : "Generated Quiz";

            int perChunk = Math.max(1, numQuestions / chunks.size());

            for (int i = 0; i < chunks.size(); i++) {
                int ask = (i == chunks.size() - 1) ? (numQuestions - all.size()) : perChunk;
                if (ask <= 0) break;

                String json = quizGenService.generateQuizJson(
                        chunks.get(i),
                        effectivePrompt,
                        ask,
                        settings
                );

                JsonNode root = om.readTree(json);
                if (root.hasNonNull("quizTitle")) quizTitle = root.get("quizTitle").asText(quizTitle);
                if (root.has("questions") && root.get("questions").isArray()) {
                    for (JsonNode q : root.get("questions")) all.add(q);
                }
            }

            if (all.size() > numQuestions) all = all.subList(0, numQuestions);

            try {
                Map<String,Object> result = Map.of(
                        "quizTitle", quizTitle,
                        "questions", all
                );
                return ResponseEntity.ok(ApiResponse.success(result));
            } catch (Exception e) {
                return ResponseEntity.status(500)
                        .body(ApiResponse.error(e.getMessage()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private String buildPromptFromSettings(AiQuizSettings s) {
        String lang = (s.getLanguage() == null || s.getLanguage().equalsIgnoreCase("Auto"))
                ? "vi" : s.getLanguage(); // default tiếng Việt

        String diff = (s.getDifficulty() == null) ? "Medium" : s.getDifficulty();
        String type = (s.getQuestionType() == null) ? "Multiple Choice" : s.getQuestionType();
        String study = (s.getStudyMode() == null) ? "Quiz" : s.getStudyMode();
        String extra = (s.getUserPrompt() == null) ? "" : s.getUserPrompt();

        return """
        Ngôn ngữ đầu ra: %s.
        Loại câu hỏi: %s. Mục tiêu sử dụng: %s.
        Độ khó mục tiêu: %s (nếu 'Mixed' thì phân bổ hợp lý easy/medium/hard).
        Yêu cầu tuỳ chỉnh: %s
        """.formatted(lang, type, study, diff, extra).trim();
    }
}