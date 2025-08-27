package com.example.springboot_education.untils;

import com.example.springboot_education.entities.QuestionType;
import jakarta.validation.ValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class QuizUtils {

    private QuizUtils() {} // chặn new

    public static String normalizeCorrectOptions(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return Arrays.stream(raw.split(","))
                .map(s -> s == null ? "" : s.trim().toUpperCase())
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.joining(","));
    }

    private static int countCorrect(String normalized) {
        if (normalized == null || normalized.isBlank()) return 0;
        return (int) normalized.chars().filter(ch -> ch == ',').count() + 1;
    }

    public static void validateByType(QuestionType type, String normalizedCorrect) {
        if (type == null) throw new ValidationException("questionType is required");

        switch (type) {
            case ONE_CHOICE -> {
                if (countCorrect(normalizedCorrect) != 1) {
                    throw new ValidationException("ONE_CHOICE must have exactly 1 correct option");
                }
            }
            case MULTI_CHOICE -> {
                if (countCorrect(normalizedCorrect) < 2) {
                    throw new ValidationException("MULTI_CHOICE must have at least 2 correct options");
                }
            }
            case TRUE_FALSE -> {
                if (!"TRUE".equals(normalizedCorrect) && !"FALSE".equals(normalizedCorrect)) {
                    throw new ValidationException("TRUE_FALSE correct option must be TRUE or FALSE");
                }
            }
            case FILL_BLANK -> {
                // thường không check correctOptions
            }
        }
    }
    public static LocalDateTime convertToLocalDateTime(Object value) {
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        } else if (value instanceof java.time.LocalDateTime) {
            return (LocalDateTime) value;
        } else if (value instanceof java.time.LocalDate) {
            return ((LocalDate) value).atStartOfDay();
        } else if (value instanceof java.sql.Date) {
            // java.sql.Date chỉ có ngày, không có giờ ⇒ dùng atStartOfDay
            return ((java.sql.Date) value).toLocalDate().atStartOfDay();
        } else if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        throw new UnsupportedOperationException("Unsupported type: " + value.getClass().getName());
    }

}
