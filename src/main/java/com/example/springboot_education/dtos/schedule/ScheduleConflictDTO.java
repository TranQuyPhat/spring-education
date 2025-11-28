package com.example.springboot_education.dtos.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO trả về thông tin chi tiết về xung đột lịch học
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleConflictDTO {

    private boolean hasConflict; // Có xung đột hay không
    private Integer targetClassId; // ID lớp đích
    private String targetClassName; // Tên lớp đích
    private int conflictCount; // Số lượng buổi học bị xung đột
    private String message; // Thông báo (dùng cho FE display)
    private List<ConflictDetail> conflicts; // Chi tiết từng xung đột

    /**
     * Chi tiết một buổi học xung đột
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictDetail {
        // Thông tin buổi học của user (lớp hiện tại)
        private Integer userSessionId;
        private LocalDate userSessionDate;
        private Integer userStartPeriod;
        private Integer userEndPeriod;
        private String userClassName;

        // Thông tin buổi học đích (lớp muốn join)
        private Integer targetSessionId;
        private LocalDate targetSessionDate;
        private Integer targetStartPeriod;
        private Integer targetEndPeriod;

        // Lý do xung đột
        private String conflictReason;
    }
}
