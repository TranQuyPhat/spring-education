package com.example.springboot_education.untils;

public class CloudinaryUtils {

    /**
     * Trích xuất publicId từ URL Cloudinary
     * Ví dụ:
     * https://res.cloudinary.com/djiinlgh2/image/upload/v1234567890/class_materials/abc123.pdf
     * => class_materials/abc123
     */
    public static String extractPublicId(String url) {
        if (url == null || !url.contains("/upload/")) {
            throw new IllegalArgumentException("Invalid Cloudinary URL: " + url);
        }

        // lấy tên file (không extension)
        String withoutExtension = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));

        // lấy folder sau /upload/
        String folder = url.split("/upload/")[1];
        folder = folder.substring(0, folder.lastIndexOf("/"));

        return folder + "/" + withoutExtension;
    }
}
