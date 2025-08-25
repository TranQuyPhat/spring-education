package com.example.springboot_education.untils;

public class FileUtils {
    public static String formatFileSize(Long sizeInBytes) {
        if (sizeInBytes == null) return "0 B";

        double size = sizeInBytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }
}
