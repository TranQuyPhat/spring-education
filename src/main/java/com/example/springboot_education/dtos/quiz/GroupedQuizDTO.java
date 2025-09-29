package com.example.springboot_education.dtos.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@AllArgsConstructor
@Getter
@Setter
public class GroupedQuizDTO {
    private List<ClassQuizDTO> classes;

    private int classPage;       // page hiện tại của lớp
    private int classTotalPages; // tổng số page lớp
}