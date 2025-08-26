package com.example.springboot_education.controllers.schedules;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.springboot_education.dtos.classschedules.LessonPlanDTO;
import com.example.springboot_education.entities.LessonPlan;

import com.example.springboot_education.services.schedules.LessonPlanService;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/auth/lesson-plans")
@RequiredArgsConstructor
public class LessonPlanController {

    private final LessonPlanService lessonPlanService;

    @PostMapping("/import/{classId}")
    public ResponseEntity<String> importLessonPlans(
            @PathVariable("classId") Integer classId,
            @RequestParam("file") MultipartFile file) {
        try {
            lessonPlanService.importLessonPlansFromExcel(classId, file);
            return ResponseEntity.ok("Import lesson plans successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<LessonPlanDTO>> getLessonPlansByClass(@PathVariable("classId") Integer classId) {
        return ResponseEntity.ok(lessonPlanService.getLessonPlansByClass(classId));
    }

    @PostMapping
    public ResponseEntity<LessonPlan> createLessonPlan(@RequestBody LessonPlan lessonPlan) {
        return ResponseEntity.ok(lessonPlanService.createLessonPlan(lessonPlan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonPlanDTO> updateLessonPlan(
            @PathVariable("id") Integer id,
            @RequestBody LessonPlanDTO dto
    ) {
        LessonPlanDTO updated = lessonPlanService.updateLessonPlan(id, dto);
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLessonPlan(@PathVariable Integer id) {
        lessonPlanService.deleteLessonPlan(id);
        return ResponseEntity.noContent().build();
    }
}
