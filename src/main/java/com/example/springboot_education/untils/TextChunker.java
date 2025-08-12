package com.example.springboot_education.untils;


import java.util.ArrayList;
import java.util.List;

public class TextChunker {

    /**
     * Chia text thành các đoạn (chunk) có độ dài tối đa maxChars ký tự.
     * Ưu tiên cắt ở chỗ xuống dòng kép (\n\n) để tránh đứt câu.
     *
     * @param text     Nội dung đầu vào
     * @param maxChars Số ký tự tối đa mỗi chunk
     * @return Danh sách các chunk
     */
    public static List<String> splitBySize(String text, int maxChars) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int i = 0;
        while (i < text.length()) {
            int end = Math.min(i + maxChars, text.length());

            // Tìm vị trí xuống dòng kép gần nhất trước end
            int lastBreak = text.lastIndexOf("\n\n", end);
            if (lastBreak <= i) {
                lastBreak = end; // Nếu không tìm thấy thì cắt tại end
            }

            String chunk = text.substring(i, lastBreak).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            i = lastBreak;
        }
        return chunks;
    }

    /**
     * Chia text theo số câu tối đa trong mỗi chunk.
     * Dùng khi muốn kiểm soát nội dung theo câu thay vì ký tự.
     *
     * @param text        Nội dung đầu vào
     * @param sentencesPerChunk số câu tối đa mỗi chunk
     * @return Danh sách các chunk
     */
    public static List<String> splitBySentences(String text, int sentencesPerChunk) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        String[] sentences = text.split("(?<=[.!?])\\s+"); // tách theo dấu câu
        StringBuilder currentChunk = new StringBuilder();
        int sentenceCount = 0;

        for (String sentence : sentences) {
            currentChunk.append(sentence).append(" ");
            sentenceCount++;

            if (sentenceCount >= sentencesPerChunk) {
                chunks.add(currentChunk.toString().trim());
                currentChunk.setLength(0);
                sentenceCount = 0;
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}
