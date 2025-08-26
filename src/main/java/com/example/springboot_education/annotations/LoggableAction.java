package com.example.springboot_education.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggableAction {

    String value(); // Loại hành động: CREATE, UPDATE, DELETE
    String entity(); // Tên entity hoặc bảng: "class", "assignment",...
    String description(); // Mô tả hành động
    String targetIdField() default ""; // ⭐ Tuỳ chọn: tên field chứa ID (nếu không phải getId)

}
