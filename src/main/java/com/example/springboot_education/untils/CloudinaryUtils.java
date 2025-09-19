package com.example.springboot_education.untils;

import java.net.URI;

public class CloudinaryUtils {

    /**
     * Trích xuất publicId từ URL Cloudinary
     * Ví dụ:
     * https://res.cloudinary.com/djiinlgh2/image/upload/v1234567890/class_materials/abc123.pdf
     * => class_materials/abc123
     */
    public static String extractPublicId(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            // Ví dụ: https://res.cloudinary.com/<cloud_name>/<resource_type>/upload/v<version>/<public_id>.<ext>
            // hoặc: https://res.cloudinary.com/<cloud_name>/raw/upload/v1234567/<public_id>
            URI uri = new URI(url);
            String path = uri.getPath(); // /raw/upload/v1758273079/tensijbaaulqevw2uxqo
            int uploadIndex = path.indexOf("/upload/");
            if (uploadIndex == -1) return null;

            String afterUpload = path.substring(uploadIndex + 8); // v1758273079/tensijbaaulqevw2uxqo
            String[] parts = afterUpload.split("/");

            // Bỏ "v123456" nếu có
            if (parts[0].matches("^v[0-9]+$")) {
                afterUpload = afterUpload.substring(parts[0].length() + 1);
            }

            // Bỏ extension nếu có
            int dotIndex = afterUpload.lastIndexOf(".");
            if (dotIndex > -1) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }

            return afterUpload; // public_id đúng dạng Cloudinary
        } catch (Exception e) {
            return null;
        }
    }
}
