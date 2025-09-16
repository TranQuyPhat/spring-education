package com.example.springboot_education.dtos.mail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DailyReportRequestDto {

    @NotBlank(message = "Email người nhận không được để trống")
    @Email(message = "Email người nhận không hợp lệ")
    private String to;

    @NotBlank(message = "Tên giáo viên không được để trống")
    private String teacherName;

    @NotBlank(message = "Tên lớp không được để trống")
    private String className;

    @NotEmpty(message = "Danh sách học sinh nộp bài không được để trống")
    private List<String> submissions;
}
