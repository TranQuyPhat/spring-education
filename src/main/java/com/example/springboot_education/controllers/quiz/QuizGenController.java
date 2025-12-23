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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/ai/quiz")
public class QuizGenController {

    private final FileParseService fileParseService;
    private final QuizGenService quizGenService;
    private final ObjectMapper om = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public QuizGenController(FileParseService fileParseService, QuizGenService quizGenService) {
        this.fileParseService = fileParseService;
        this.quizGenService = quizGenService;
    }

    @PostMapping(value = "/generate-from-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> generateFromFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "settings", required = false) String settingsJson) {
        try {
            // Parse settings from JSON string
            AiQuizSettings settings = (settingsJson != null && !settingsJson.isBlank())
                    ? om.readValue(settingsJson, AiQuizSettings.class)
                    : new AiQuizSettings();

            int numQuestions = settings.getNumQuestions() != null ? settings.getNumQuestions() : 10;
            if (numQuestions < 1)
                numQuestions = 1;
            if (numQuestions > 100)
                numQuestions = 100; // guard

            String text = fileParseService.parseToPlainText(file);

            // ========================================
            // STEP 1: VALIDATION (Two-Step Process)
            // ========================================
            // If user provided a title, validate it with AI using sample content
            if (settings.getQuizTitle() != null && !settings.getQuizTitle().isBlank()) {
                String sampleContent = text.length() > 3000 ? text.substring(0, 3000) : text;
                boolean isRelevant = quizGenService.validateTitleWithAI(settings.getQuizTitle(), sampleContent);

                if (!isRelevant) {
                    return ResponseEntity.status(400).body(Map.of(
                            "error", "Tiêu đề không liên quan đến nội dung file",
                            "userTitle", settings.getQuizTitle(),
                            "suggestion", "Vui lòng kiểm tra lại file hoặc tiêu đề"));
                }
            }

            // ========================================
            // STEP 2: GENERATION (Chunking + Parallel)
            // ========================================
            List<String> chunks = TextChunker.splitBySize(text, 12000);
            if (chunks.isEmpty())
                chunks = List.of(text);
            String effectivePrompt = buildPromptFromSettings(settings);
            String quizTitle = (settings.getQuizTitle() != null && !settings.getQuizTitle().isBlank())
                    ? settings.getQuizTitle()
                    : "Generated Quiz";

            int perChunk = Math.max(1, numQuestions / chunks.size());

            // --- Xử lý song song (Parallel) ---
            final List<String> finalChunks = chunks;
            final String finalEffectivePrompt = effectivePrompt;
            final int finalNumQuestions = numQuestions;
            List<CompletableFuture<List<JsonNode>>> futures = new ArrayList<>();
            String finalQuizTitle = quizTitle;

            // Store first chunk's response to extract quizTitle (avoid duplicate API call)
            JsonNode[] firstChunkResponse = new JsonNode[1];

            for (int i = 0; i < finalChunks.size(); i++) {
                final int index = i;
                final int ask = (i == finalChunks.size() - 1) ? (finalNumQuestions - (index * perChunk)) : perChunk;

                if (ask <= 0)
                    break;

                CompletableFuture<List<JsonNode>> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        String json = quizGenService.generateQuizJson(
                                finalChunks.get(index),
                                finalEffectivePrompt,
                                ask,
                                settings);
                        JsonNode root = om.readTree(json);

                        // Store first chunk's full response for title extraction
                        if (index == 0) {
                            firstChunkResponse[0] = root;
                        }

                        List<JsonNode> questions = new ArrayList<>();
                        if (root.has("questions") && root.get("questions").isArray()) {
                            for (JsonNode q : root.get("questions"))
                                questions.add(q);
                        }
                        return questions;
                    } catch (Exception e) {
                        return new ArrayList<>();
                    }
                }, executorService);

                futures.add(future);
            }

            // Đợi tất cả requests hoàn thành
            List<JsonNode> all = new ArrayList<>();
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            allFutures.join();

            // Collect kết quả từ tất cả futures
            for (CompletableFuture<List<JsonNode>> future : futures) {
                all.addAll(future.join());
            }

            // --- OPTION B: Validate Title Relevance ---
            // Lấy title từ response đầu tiên (đã lưu từ parallel processing, KHÔNG gọi API
            // lại)
            String generatedTitle = finalQuizTitle;
            if (firstChunkResponse[0] != null) {
                try {
                    if (firstChunkResponse[0].hasNonNull("quizTitle")) {
                        generatedTitle = firstChunkResponse[0].get("quizTitle").asText();
                    }
                } catch (Exception e) {
                    // Keep default
                }
            }

            // Use generated title (AI already validated user title in STEP 1)
            quizTitle = generatedTitle;

            if (all.size() > numQuestions)
                all = all.subList(0, numQuestions);

            try {
                Map<String, Object> result = Map.of(
                        "quizTitle", quizTitle,
                        "questions", all);
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
                ? "vi"
                : s.getLanguage(); // default tiếng Việt

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