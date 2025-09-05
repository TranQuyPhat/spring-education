package com.example.springboot_education.services.quiz;

import com.example.springboot_education.dtos.quiz.QuestionResponse;
import com.example.springboot_education.dtos.quiz.teacher.QuestionTeacherDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service xử lý file PDF/TXT -> trích text -> gọi LLM nhiều lần theo chunk -> gộp JSON theo schema QuestionResponse.
 */
@Service
public class DocumentService {

    private final Client client;
    private final ObjectMapper objectMapper;

    public DocumentService(Client client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Value("${GEMINI_MODEL:gemini-2.5-flash}")
    private String model;

    // Giới hạn/tuỳ chọn
    private static final int MAX_PAGES = 200;            // tránh PDF quá dài
    private static final int MAX_CHARS_PER_CHUNK = 12000; // tuỳ theo model/context
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB

    // Public API
    public QuestionResponse processFile(MultipartFile file) throws IOException {
        validateFile(file);

        String ext = getExtension(file);
        String text;
        switch (ext) {
            case "pdf" -> text = extractTextFromPdf(file);
            case "txt" -> text = new String(file.getBytes(), StandardCharsets.UTF_8);
            default -> throw new IllegalArgumentException("Chỉ hỗ trợ PDF hoặc TXT.");
        }

        if (text == null || text.isBlank()) {
            throw new IOException("Không thể trích xuất văn bản từ file (PDF rỗng hoặc là bản scan/ảnh).");
        }

        return extractQuestionsFromTextChunked(text);
    }

    // ====== Extraction ======

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            if (document.isEncrypted()) {
                throw new IOException("PDF đang được mã hóa (encrypted) — không thể trích xuất văn bản.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(Math.min(document.getNumberOfPages(), MAX_PAGES));

            String raw = stripper.getText(document);
            String cleaned = cleanPdfText(raw);
            if (cleaned.isBlank()) {
                throw new IOException("Không tìm thấy văn bản trong PDF (có thể PDF là bản scan/ảnh).");
            }
            return cleaned;
        } catch (IOException e) {
            throw new IOException("Lỗi khi đọc file PDF: " + e.getMessage(), e);
        }
    }

    private String cleanPdfText(String input) {
        if (input == null) return "";
        String s = input;
        // Remove soft hyphen & zero-width characters
        s = s.replace("\u00AD", "")  // soft hyphen
                .replace("\u200B", "")  // zero-width space
                .replace("\uFEFF", ""); // BOM

        // Common ligatures
        s = s.replace("ﬁ", "fi").replace("ﬂ", "fl");

        // Join hyphenated words at line breaks: "từ-\n tiếp" -> "từtiếp"
        s = s.replaceAll("([\\p{L}\\p{N}])\\-\\R([\\p{L}\\p{N}])", "$1$2");

        // Normalize newlines
        s = s.replace("\r\n", "\n").replace("\r", "\n");

        // Collapse whitespace runs (except newline)
        s = s.replaceAll("[ \\t\\x0B\\f]+", " ");

        // Trim empty lines
        StringBuilder out = new StringBuilder();
        for (String line : s.split("\n")) {
            String ln = line.trim();
            if (!ln.isEmpty()) {
                out.append(ln).append("\n");
            }
        }
        return out.toString().trim();
    }

    // ====== Chunking & LLM calls ======

    private QuestionResponse extractQuestionsFromTextChunked(String fullText) {
        List<String> chunks = splitForModel(fullText, MAX_CHARS_PER_CHUNK);
        List<QuestionResponse> partials = new ArrayList<>(chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            QuestionResponse r = extractQuestionsFromTextSingleChunk(chunk, i + 1, chunks.size());
            partials.add(r);
        }

        return mergeQuestionResponses(partials);
    }

    private List<String> splitForModel(String text, int maxChars) {
        if (text.length() <= maxChars) return List.of(text);

        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + maxChars);

            // Cố gắng cắt ở ranh giới dòng
            if (end < text.length()) {
                int lastNewline = text.lastIndexOf('\n', end);
                if (lastNewline > start + maxChars * 0.6) {
                    end = lastNewline + 1;
                }
            }

            parts.add(text.substring(start, end));
            start = end;
        }
        return parts;
    }

    private QuestionResponse extractQuestionsFromTextSingleChunk(String textChunk, int idx, int total) {
        try {
            // System instruction
            String systemInstruction =
                    "Bạn là AI chuyên trích xuất và chuẩn hoá câu hỏi trắc nghiệm.\n" +
                            "- GIỮ NGUYÊN 100% nội dung câu hỏi & đáp án (không bịa).\n" +
                            "- Nếu có công thức, bọc bằng LaTeX $...$.\n" +
                            "- Nếu không chắc đáp án đúng: đặt correctAnswer = null.\n" +
                            "- Trả về JSON đúng schema DUY NHẤT, không thêm giải thích.";

            // Schema: option
            Schema optionSchema = Schema.builder()
                    .type(Type.Known.OBJECT)
                    .properties(Map.of(
                            "optionLabel", Schema.builder().type(Type.Known.STRING).build(),
                            "optionText",  Schema.builder().type(Type.Known.STRING).build()
                    ))
                    .required(List.of("optionLabel", "optionText"))
                    .build();

            // Schema: question
            Schema questionSchema = Schema.builder()
                    .type(Type.Known.OBJECT)
                    .properties(Map.of(
                            "questionType", Schema.builder().type(Type.Known.STRING)
                                    .enum_(List.of("ONE_CHOICE","MULTI_CHOICE","TRUE_FALSE","FILL_BLANK")).build(),
                            "questionText",  Schema.builder().type(Type.Known.STRING).build(),
                            "score",         Schema.builder().type(Type.Known.NUMBER).default_(1).build(),
                            "correctOption", Schema.builder().type(Type.Known.STRING).nullable(true).build(),
                            "options",       Schema.builder().type(Type.Known.ARRAY).items(optionSchema).build(),
                            "correctAnswerTexts", Schema.builder().type(Type.Known.ARRAY)
                                    .items(Schema.builder().type(Type.Known.STRING).build()).nullable(true).build(),
                            "correctAnswerRegex", Schema.builder().type(Type.Known.STRING).nullable(true).build(),
                            "caseSensitive", Schema.builder().type(Type.Known.BOOLEAN).default_(false).build(),
                            "trimWhitespace", Schema.builder().type(Type.Known.BOOLEAN).default_(false).build()
                    ))
                    // yêu cầu tối thiểu, đừng ép quá chặt để model dễ thở
                    .required(List.of("questionType","questionText","options"))
                    // cập nhật ordering cho đúng field hiện có
                    .propertyOrdering(List.of(
                            "questionType","questionText","score","options",
                            "correctOption","correctAnswerTexts","correctAnswerRegex",
                            "caseSensitive","trimWhitespace"
                    ))
                    .build();

            // Schema: response
            Schema responseSchema = Schema.builder()
                    .type(Type.Known.OBJECT)
                    .properties(Map.of(
                            "questions", Schema.builder().type(Type.Known.ARRAY).items(questionSchema).build()
                    ))
                    .required(List.of("questions"))
                    .build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(Content.builder()
                            .role("system")
                            .parts(List.of(Part.builder().text(systemInstruction).build()))
                            .build())
                    .responseMimeType("application/json")
                    .responseSchema(responseSchema)
                    .temperature(0.1f)
                    .topK(30f)
                    .topP(0.8f)
                    .build();

            String prompt = createPromptForChunk(textChunk, idx, total);

            // Tuỳ SDK: một số phiên bản dùng (model, promptString, config)
            GenerateContentResponse response = client.models.generateContent(model, prompt, config);
            String aiJson = response.text();

            return parseAiResponse(aiJson);

        } catch (Exception e) {
            // Trả về rỗng để tiếp tục gộp; hoặc ném lỗi tuỳ chiến lược
            return new QuestionResponse(Collections.emptyList());
        }
    }

    private String createPromptForChunk(String text, int idx, int total) {
        return String.format("""
[PHẦN %d/%d]
                Bạn là AI trích xuất câu hỏi trắc nghiệm từ văn bản PDF/TXT đã được OCR.
                
                NHIỆM VỤ:
                - Với MỖI câu hỏi, đọc loại câu hỏi (nếu không ghi rõ, mặc định ONE_CHOICE).
                - Đọc các phương án (A., B., C., D. hoặc TRUE/FALSE).
                - Tìm dòng "Đáp án:" NGAY SAU câu hỏi (hoặc sau các lựa chọn) để lấy đáp án từ NGUỒN.
                - KHÔNG tự suy luận/giải toán. Chỉ dùng giá trị sau "Đáp án:".
                
                CÁCH HIỂU "Đáp án:" THEO TỪNG LOẠI:
                1) ONE_CHOICE
                   - Dạng: "Đáp án: A" (một trong A/B/C/D).
                   - Output: correctOption = "A|B|C|D".
                2) MULTI_CHOICE
                   - Dạng: "Đáp án: A, C, D" (phân tách bằng dấu phẩy, khoảng trắng, hoặc "và/hoặc").
                   - Chuẩn hoá thứ tự ABCD, không trùng lặp.
                   - Output: correctOption = "A,B,D" (chuỗi chữ cái, ngăn bởi dấu phẩy, không khoảng trắng).
                3) TRUE_FALSE
                   - Dạng: "Đáp án: TRUE" hoặc "Đáp án: FALSE" (chấp nhận “Đúng/TRUE”, “Sai/FALSE”).
                   - Output: correctOption = "TRUE" hoặc "FALSE".
                4) FILL_BLANK
                   - Dạng văn bản: "Đáp án: H2O; nước; …" → correctAnswerTexts = ["H2O","nước",...], case-insensitive (caseSensitive=false), trimWhitespace=true.
                   - Dạng biểu thức chính quy: "Đáp án (REGEX): <mẫu>" → correctAnswerRegex = "<mẫu>".
                   - Các câu FILL_BLANK KHÔNG dùng correctOption; để correctOption = "" (chuỗi rỗng), options = [].
                
                QUY TẮC CHUNG:
                - Nếu KHÔNG tìm thấy dòng "Đáp án:" → coi là chưa có đáp án:\s
                  - ONE_CHOICE/MULTI_CHOICE/TRUE_FALSE: correctOption = null
                  - FILL_BLANK: correctAnswerTexts = [], correctAnswerRegex = null, correctOption = ""
                - Chuẩn hoá: bỏ khoảng trắng thừa, giữ nguyên công thức trong $...$ nếu xuất hiện.
                - Chỉ trả về JSON ĐÚNG SCHEMA, không kèm giải thích.
                
                SCHEMA JSON TRẢ RA:
                {
                  "questions": [
                    {
                      "questionType": "ONE_CHOICE" | "MULTI_CHOICE" | "TRUE_FALSE" | "FILL_BLANK",
                      "questionText": "string",
                      "score": number,
                      "correctOption": "A|B|C|D|null|\\"A,B,D\\"|\\"TRUE\\"|\\"FALSE\\"|\\"\\"" ,
                      "options": [
                        { "optionLabel": "A"|"B"|"C"|"D"|"TRUE"|"FALSE", "optionText": "string" }
                      ],
                      "correctAnswerTexts": [ "string", ... ] | null,
                      "correctAnswerRegex": "string | null",
                      "caseSensitive": boolean,
                      "trimWhitespace": boolean
                    }
                  ]
                }
                
                VĂN BẢN:
                %s
                
""", idx, total, text);
    }

    private QuestionResponse parseAiResponse(String aiResponse) {
        try {
            if (aiResponse == null || aiResponse.isBlank()) {
                return new QuestionResponse(Collections.emptyList());
            }
            return objectMapper.readValue(aiResponse, QuestionResponse.class);
        } catch (JsonProcessingException e) {
            // Nếu model in kèm text, thử bóc JSON bằng heuristic đơn giản
            try {
                String json = extractFirstJsonObject(aiResponse);
                if (json != null) {
                    return objectMapper.readValue(json, QuestionResponse.class);
                }
            } catch (Exception ignore) {}

            return new QuestionResponse(Collections.emptyList());
        }
    }

    private QuestionResponse mergeQuestionResponses(List<QuestionResponse> parts) {
        List<QuestionTeacherDTO> all = parts.stream()
                .filter(Objects::nonNull)
                .flatMap(r -> {
                    if (r.getQuestions() == null) return Stream.<QuestionTeacherDTO>empty();
                    return r.getQuestions().stream();
                })
                .collect(Collectors.toList());
        return new QuestionResponse(all);
    }

    // ====== Utils ======

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File rỗng hoặc không tồn tại.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File vượt quá dung lượng cho phép (tối đa 10MB).");
        }
        String ext = getExtension(file);
        if (!ext.equals("pdf") && !ext.equals("txt")) {
            throw new IllegalArgumentException("Định dạng không hỗ trợ. Chỉ chấp nhận PDF hoặc TXT.");
        }
    }

    private String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) return "";
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Thử bóc JSON đầu tiên trong chuỗi (khi model lỡ in kèm giải thích).
     */
    private String extractFirstJsonObject(String s) {
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1);
        }
        return null;
    }
}
