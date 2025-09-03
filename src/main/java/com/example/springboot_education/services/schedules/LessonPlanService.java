package com.example.springboot_education.services.schedules;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;


import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.springboot_education.dtos.classschedules.LessonPlanCreateDTO;
import com.example.springboot_education.dtos.classschedules.LessonPlanDTO;
import com.example.springboot_education.entities.ClassEntity;
import com.example.springboot_education.entities.LessonPlan;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.schedules.LessonPlanRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class LessonPlanService {

    private final LessonPlanRepository lessonPlanRepository;
    private final ClassRepository classRepository;
    
    
    private LessonPlanDTO toDTO(LessonPlan plan) {
        return new LessonPlanDTO(
                plan.getId(),
                plan.getSessionNumber(),
                plan.getTitle(),
                plan.getDescription()
        );
    }

    public List<LessonPlanDTO> getLessonPlansByClass(Integer classId) {
        return lessonPlanRepository.findByClassRoom_IdOrderBySessionNumberAsc(classId)
                .stream()
                .map(this::toDTO) // dùng hàm riêng
                .collect(Collectors.toList());
    }

    public LessonPlan createLessonPlan(LessonPlan lessonPlan) {
        return lessonPlanRepository.save(lessonPlan);
    }
    public void importLessonPlansFromExcel(Integer classId, MultipartFile file) throws IOException {
        // lấy class từ DB
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class"));

        // đọc file excel
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); 

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { 
                Row row = sheet.getRow(i);
                if (row == null) continue;

                LessonPlanCreateDTO dto = new LessonPlanCreateDTO();

                // cột 0: sessionNumber
                Cell sessionCell = row.getCell(0);
                if (sessionCell == null || sessionCell.getCellType() != CellType.NUMERIC) {
                    throw new HttpException("Invalid session number at row " + (i + 1), HttpStatus.BAD_REQUEST);
                }
                dto.setSessionNumber((int) sessionCell.getNumericCellValue());

                // cột 1: title
                Cell titleCell = row.getCell(1);
                if (titleCell == null || titleCell.getCellType() == CellType.BLANK) {
                    throw new HttpException("Title is required at row " + (i + 1), HttpStatus.BAD_REQUEST);
                }
                dto.setTitle(titleCell.getStringCellValue());

                // cột 2: description (optional)
                Cell descCell = row.getCell(2);
                if (descCell != null && descCell.getCellType() != CellType.BLANK) {
                    dto.setDescription(descCell.getStringCellValue());
                }

                // map DTO -> Entity
                LessonPlan plan = new LessonPlan();
                plan.setClassRoom(classEntity);
                plan.setSessionNumber(dto.getSessionNumber());
                plan.setTitle(dto.getTitle());
                plan.setDescription(dto.getDescription());

                lessonPlanRepository.save(plan);
            }
        }
    }
    public LessonPlanDTO updateLessonPlan(Integer id, LessonPlanDTO dto) {
        LessonPlan existing = lessonPlanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lesson plan"));

        existing.setSessionNumber(dto.getSessionNumber());
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());

        LessonPlan saved = lessonPlanRepository.save(existing);

        // convert entity -> dto
        return toDTO(saved);
    }

  public void deleteLessonPlan(Integer id) {
    LessonPlan existing = lessonPlanRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Lesson plan not found with id " + id));
    lessonPlanRepository.delete(existing);
}

}
