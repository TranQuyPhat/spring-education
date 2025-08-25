package com.example.springboot_education.untils;

import java.time.Duration;
import java.time.Instant;

public class TimeAgoUtil {

    public static String timeAgo(Instant past) {
        Instant now = Instant.now();
        long seconds = Duration.between(past, now).getSeconds();

        if (seconds < 60) return "vừa xong";
        if (seconds < 3600) return (seconds / 60) + " phút trước";
        if (seconds < 86400) return (seconds / 3600) + " giờ trước";
        return (seconds / 86400) + " ngày trước";
    }
}
