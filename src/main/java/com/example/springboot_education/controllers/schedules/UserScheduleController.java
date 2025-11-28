package com.example.springboot_education.controllers.schedules;

import com.example.springboot_education.annotations.CurrentUser;
import com.example.springboot_education.dtos.ApiResponse;
import com.example.springboot_education.dtos.schedule.UserScheduleDTO;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.services.schedules.UserScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class UserScheduleController {
    private final UserScheduleService userScheduleService;

    /**
     * Lấy thời khoá biểu của tuần hiện tại (hoặc tuần bất kỳ nếu cung cấp date)
     * 
     * Response chứa: weekStartDate, weekEndDate, previousWeekStartDate,
     * nextWeekStartDate
     * FE sẽ dùng các ngày này để navigate qua lại giữa các tuần
     * 
     * @param currentUser User hiện tại (từ JWT token)
     * @param date        Ngày bất kỳ trong tuần cần lấy (format: YYYY-MM-DD), mặc
     *                    định là hôm nay
     * @return Thời khoá biểu của tuần chứa ngày được chỉ định
     * 
     *         Ví dụ:
     *         GET /api/schedules/week → Tuần hiện tại
     *         GET /api/schedules/week?date=2025-12-25 → Tuần chứa 25/12/2025
     */
    @GetMapping("/week")
    public ResponseEntity<ApiResponse<UserScheduleDTO>> getScheduleByWeek(
            @CurrentUser Users currentUser,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
        }

        try {
            LocalDate queryDate = (date != null) ? date : LocalDate.now();
            UserScheduleDTO schedule = userScheduleService.getScheduleByWeek(currentUser, queryDate);
            return ResponseEntity.ok(ApiResponse.success("Schedule retrieved successfully", schedule));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }
}
