package com.example.springboot_education.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot_education.dtos.assignmentDTOs.AssignmentResponseDto;
import com.example.springboot_education.dtos.assignmentDTOs.CreateAssignmentRequestDto;
import com.example.springboot_education.dtos.assignmentDTOs.UpdateAssignmentRequestDto;
import com.example.springboot_education.services.AssignmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping()
    public List<AssignmentResponseDto> getAllAssignments() {
        return assignmentService.getAllAssignments();
    }

    @GetMapping("/{id}")
    public AssignmentResponseDto getAssignmentById(@PathVariable("id") Long id) {
        return assignmentService.getAssignmentById(id);
    }

    @PostMapping()
    public AssignmentResponseDto createAssignment(@RequestBody @Valid CreateAssignmentRequestDto createAssignmentRequestDto) {
        return assignmentService.createAssignment(createAssignmentRequestDto);
    }

    @PatchMapping("/{id}")
    public AssignmentResponseDto updateAssignment(@PathVariable("id") Long id, @RequestBody @Valid UpdateAssignmentRequestDto updateAssignmentRequestDto) {
        return assignmentService.updateAssignment(id, updateAssignmentRequestDto);
    }

    @DeleteMapping("/{id}")
    public void deleteAssignment(@PathVariable("id") Long id) {
        assignmentService.deleteAssignment(id);
    }

}
