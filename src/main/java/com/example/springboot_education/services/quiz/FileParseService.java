package com.example.springboot_education.services.quiz;


import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.InputStream;
import java.util.StringJoiner;

@Service
public class FileParseService {

    private final Tika tika = new Tika();

    /**
     * Parse file upload (PDF/DOCX/…) -> plain text đã được normalize.
     */
    public String parseToPlainText(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
        String mime = tika.detect(filename);

        try (InputStream is = file.getInputStream()) {
            // Nếu là DOCX và bạn muốn văn bản "sạch" hơn => POI
            if (filename.toLowerCase().endsWith(".docx")) {
                try {
                    return normalizeText(parseDocxWithPOI(is));
                } catch (Exception ignore) {
                    // Fallback sang Tika bên dưới
                }
            }

            // Mặc định: Tika (tự detect PDF, DOCX, TXT, PPTX…)
            AutoDetectParser parser = new AutoDetectParser();
            ContentHandler handler = new BodyContentHandler(-1); // unlimited
            Metadata metadata = new Metadata();
            parser.parse(is, handler, metadata);
            return normalizeText(handler.toString());
        }
    }

    /**
     * Fallback/tuỳ chọn: parse DOCX bằng Apache POI để kiểm soát tốt hơn.
     */
    private String parseDocxWithPOI(InputStream in) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(in)) {
            StringJoiner sj = new StringJoiner("\n");
            doc.getParagraphs().forEach(p -> {
                String text = p.getText();
                if (text != null && !text.isBlank()) sj.add(text);
            });
            // (Tuỳ chọn) Duyệt bảng, header/footer…
            // doc.getTables() ... // ghép thêm nếu cần
            return sj.toString();
        }
    }

    /**
     * Làm sạch text: bỏ rác, chuẩn hoá khoảng trắng, gom dòng.
     */
    private String normalizeText(String raw) {
        if (raw == null) return "";
        String s = raw.replace("\r", "\n");
        s = s.replaceAll("\\t", " ");
        s = s.replaceAll(" +", " ");
        s = s.replaceAll("\\n{3,}", "\n\n"); // tối đa 2 dòng trống liên tiếp

        // Bỏ các dòng quá ngắn (số trang, dấu lẻ…)
        StringBuilder out = new StringBuilder();
        for (String line : s.split("\\n")) {
            String t = line.strip();
            if (t.length() <= 2) continue;
            out.append(t).append('\n');
        }
        return out.toString().strip();
    }
}