package com.example.springboot_education.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot_education.dtos.classschedules.ClassScheduleCreateDTO;
import com.example.springboot_education.dtos.classschedules.ClassScheduleResponseDTO;
import com.example.springboot_education.dtos.classschedules.ClassScheduleUpdateDTO;
import com.example.springboot_education.services.ClassScheduleService;

@RestController
@RequestMapping("/api/class-schedules")
public class ClassScheduleController {

    private final ClassScheduleService service;

    public ClassScheduleController(ClassScheduleService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClassScheduleResponseDTO> getAll() {
        return service.getAll();
    }

    @PostMapping
    public ClassScheduleResponseDTO create(@RequestBody ClassScheduleCreateDTO dto) {
        return service.create(dto);
    }

    @PatchMapping("/{id}")
    public ClassScheduleResponseDTO update(@PathVariable("id") Integer id, @RequestBody ClassScheduleUpdateDTO dto) {
    return service.update(id, dto);
}


}
