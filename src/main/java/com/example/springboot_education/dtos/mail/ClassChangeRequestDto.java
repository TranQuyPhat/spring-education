package com.example.springboot_education.dtos.mail;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClassChangeRequestDto {

    @NotBlank(message = "Email người nhận không được để trống")
    @Email(message = "Email người nhận không hợp lệ")
    private String to;

    @NotBlank(message = "Tên người nhận không được để trống")
    private String fullName;

    @NotBlank(message = "Tên lớp không được để trống")
    private String className;

    @NotBlank(message = "Loại thông báo không được để trống (cancel | makeup)")
    @Pattern(regexp = "^(cancel|makeup)$", message = "Loại thông báo chỉ được là 'cancel' hoặc 'makeup'")
    private String type;

    @NotNull(message = "Ngày không được để trống")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate date;

    private String note;
}
