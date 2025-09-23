package com.example.springboot_education.controllers;

import com.example.springboot_education.dtos.assignmentDTOs.AssignmentResponseDto;
import com.example.springboot_education.dtos.assignmentDTOs.CreateAssignmentRequestDto;
import com.example.springboot_education.dtos.assignmentDTOs.PaginatedAssignmentResponseDto;
import com.example.springboot_education.dtos.assignmentDTOs.UpdateAssignmentRequestDto;
import com.example.springboot_education.dtos.materialDTOs.DownloadFileDTO;
import com.example.springboot_education.entities.Assignment;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.services.assignment.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@Tag(name = "Assignment Management", description = "APIs for managing assignment with role-based access control")
@SecurityRequirement(name = "bearerAuth")
public class AssignmentController {
    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @Operation(summary = "Get all assignments", description = "Retrieve all assignments from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignments retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssignmentResponseDto.class)))
    })
    @GetMapping()
    public List<AssignmentResponseDto> getAllAssignments() {
        return assignmentService.getAllAssignments();
    }

    @Operation(summary = "Get assignment by ID", description = "Retrieve a specific assignment by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment found successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssignmentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Assignment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {
                                "status": 404,
                                "messages": [
                                    "Assignment with ID 999 not found"
                                ],
                                "error": "Not Found"
                            }
                            """)))
    })
    @GetMapping("/{id}")
    public AssignmentResponseDto getAssignmentById(@PathVariable("id") Integer id) {
        return assignmentService.getAssignmentById(id);
    }

    // @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // public ResponseEntity<AssignmentResponseDto> createAssignment(
    // @Valid @RequestParam("classId") Integer classId,
    // @Valid @RequestParam("title") String title,
    // @Valid @RequestParam(value = "description", required = false) String
    // description,
    // @Valid @RequestParam(value = "dueDate") @DateTimeFormat(iso =
    // DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
    // @Valid @RequestParam("maxScore") BigDecimal maxScore,
    // @RequestPart(value = "file", required = false) MultipartFile file) throws
    // IOException {
    // CreateAssignmentRequestDto dto = new CreateAssignmentRequestDto();
    // dto.setClassId(classId);
    // dto.setTitle(title);
    // dto.setDescription(description);
    // dto.setDueDate(dueDate);
    // dto.setMaxScore(maxScore);

    // return ResponseEntity.ok(assignmentService.createAssignmentWithFile(dto,
    // file));
    // }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentResponseDto> createAssignment(
            @Valid @ModelAttribute CreateAssignmentRequestDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(assignmentService.createAssignmentWithFile(dto, file));
    }

    // @PatchMapping(value = "/{id}", consumes =
    // MediaType.MULTIPART_FORM_DATA_VALUE)
    // public ResponseEntity<AssignmentResponseDto> updateAssignment(
    // @PathVariable("id") Integer id,
    // @Valid @RequestParam Integer classId,
    // @Valid @RequestParam String title,
    // @Valid @RequestParam(required = false) String description,
    // @Valid @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // LocalDateTime dueDate,
    // @Valid @RequestParam BigDecimal maxScore,
    // @RequestPart(value = "file", required = false) MultipartFile file) throws
    // IOException {
    // UpdateAssignmentRequestDto dto = new UpdateAssignmentRequestDto();
    // dto.setClassId(classId);
    // dto.setTitle(title);
    // dto.setDescription(description);
    // dto.setDueDate(dueDate);
    // dto.setMaxScore(maxScore);

    // AssignmentResponseDto updated = assignmentService.updateAssignment(id, dto,
    // file);
    // return ResponseEntity.ok(updated);
    // }
    @Operation(summary = "Update an assignment", description = "Update an existing assignment and optionally update its file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssignmentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Assignment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {
                                "status": 404,
                                "messages": [
                                    "Assignment with ID 999 not found"
                                ],
                                "error": "Not Found"
                            }
                            """)))
    })
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentResponseDto> updateAssignment(
            @PathVariable Integer id,
            @Valid @ModelAttribute UpdateAssignmentRequestDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        AssignmentResponseDto updated = assignmentService.updateAssignment(id, dto, file);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete assignment", description = "Permanently delete an assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Assignment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Assignment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {
                                "status": 404,
                                "messages": [
                                    "Assignment with ID 999 not found"
                                ],
                                "error": "Not Found"
                            }
                            """)))
    })
    @DeleteMapping("/{id}")
    public void deleteAssignment(@PathVariable("id") Integer id) {
        assignmentService.deleteAssignment(id);
    }

    @Operation(summary = "Get assignments by class", description = "Retrieve all assignments for a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignments retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssignmentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Class not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {
                                "status": 404,
                                "messages": [
                                    "Class with id: 1 not found"
                                ],
                                "error": "Not Found"
                            }
                            """)))
    })
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<AssignmentResponseDto>> getAssignmentsByClassId(
            @PathVariable("classId") Integer classId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByClassId(classId));
    }

    @Operation(
            summary = "Get paginated assignments by class",
            description = "Retrieve assignments of a specific class with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated assignments retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaginatedAssignmentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                {
                                    "status": 400,
                                    "messages": [
                                        "page: Page number must be greater than 0"
                                    ],
                                    "error": "Bad Request"
                                }
                                """))),
            @ApiResponse(responseCode = "404", description = "Class not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                {
                                    "status": 404,
                                    "messages": [
                                        "Class with id: 1 not found"
                                    ],
                                    "error": "Not Found"
                                }
                                """)))
    })
    @GetMapping("/class/{classId}/paging")
    public PaginatedAssignmentResponseDto getAssignmentsByClassIdPaginated(
            @PathVariable("classId") Integer classId,
            @Parameter(description = "Page number (1-based)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "5")
            @RequestParam(defaultValue = "5") int size) {

        // Log ra để debug
        System.out.println("classId: " + classId + ", page: " + page + ", size: " + size);

        return assignmentService.getAssignmentsByClassIdPaginated(classId, page, size);
    }

    // Tải file bài tập
    @Operation(summary = "Download assignment file", description = "Redirects to the file URL for download")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirect to download URL"),
            @ApiResponse(responseCode = "404", description = "File not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {
                                "status": 404,
                                "messages": [
                                    "File not found"
                                ],
                                "error": "Not Found"
                            }
                            """)))
    })
    @GetMapping("/{assignmentId}/download")
    public ResponseEntity<Void> downloadAssignment(@PathVariable("assignmentId") Integer assignmentId) {
        Assignment assignment = assignmentService.getAssignmentEntityById(assignmentId);

        if (assignment.getFilePath() == null) {
            throw new EntityNotFoundException("File not found");
        }

        // Thêm tham số fl_attachment để Cloudinary buộc tải file
        String fileUrl = assignment.getFilePath() + "?fl_attachment";

        return ResponseEntity.status(HttpStatus.FOUND) // 302 redirect
                .location(URI.create(fileUrl))
                .build();
    }

    // Công bố điểm
    @Operation(summary = "Publish assignment", description = "Publish the assignment to students")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment published successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssignmentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Assignment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {
                                "status": 404,
                                "messages": [
                                    "Assignment with ID 999 not found"
                                ],
                                "error": "Not Found"
                            }
                            """)))
    })
    @PatchMapping("/{id}/publish")
    public ResponseEntity<AssignmentResponseDto> publishAssignment(@PathVariable("id") Integer id) {
        AssignmentResponseDto updated = assignmentService.publishAssignment(id);
        return ResponseEntity.ok(updated);
    }


}
