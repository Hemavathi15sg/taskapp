package com.techwave.taskapp.controller;

import com.techwave.taskapp.dto.CreateTaskRequest;
import com.techwave.taskapp.dto.TaskResponse;
import com.techwave.taskapp.entity.Task;
import com.techwave.taskapp.service.interfaces.ITaskService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TaskController - HTTP request handler for task operations.
 * 
 * Layered Architecture: Controller Layer
 * Responsibilities: HTTP handling only
 * - Request validation (via @Valid annotation)
 * - HTTP response formulation (via ResponseEntity)
 * - DTO conversion
 * - NO business logic (delegated to TaskService)
 * 
 * Constructor Injection: TaskService injected via constructor.
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    private final ITaskService taskService;
    
    /**
     * Constructor injection for TaskService.
     * 
     * SOLID: Dependency Inversion Principle - depends on ITaskService abstraction
     * Architecture: Constructor injection ensures immutability and testability
     * 
     * @param taskService Task business logic service interface
     */
    public TaskController(ITaskService taskService) {
        this.taskService = taskService;
        log.debug("TaskController initialized with TaskService");
    }
    
    /**
     * Create a new task.
     * 
     * HTTP Endpoint: POST /api/tasks
     * 
     * Request:
     *   - Content-Type: application/json
     *   - Body: CreateTaskRequest (title required, description optional)
     * 
     * Response (HTTP 201):
     *   - Location: /api/tasks/{id}
     *   - Body: TaskResponse with id, title, description, status (PENDING), createdAt
     * 
     * Error Responses:
     *   - HTTP 400: Validation errors (missing/invalid title, description too long, etc.)
     *   - HTTP 500: Internal server error
     * 
     * Validation (per FRD):
     * - @Valid annotation triggers Spring validation on CreateTaskRequest
     * - Validates: title required/non-empty, max lengths
     * - ValidationException handled by GlobalExceptionHandler
     * 
     * @param request ValidRequest DTO with title and optional description
     * @return ResponseEntity with HTTP 201 and TaskResponse body
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        log.info("Received POST /api/tasks request with title: {}", request.getTitle());
        log.debug("Request body - title: {}, description: {}", request.getTitle(), request.getDescription());
        
        // Delegate business logic to service layer (no logic here)
        Task createdTask = taskService.createTask(request);
        
        // Convert entity to response DTO
        TaskResponse response = TaskResponse.builder()
                .id(createdTask.getId())
                .title(createdTask.getTitle())
                .description(createdTask.getDescription())
                .status(createdTask.getStatus().getValue())
                .createdAt(createdTask.getCreatedAt())
                .build();
        
        log.info("Returning HTTP 201 response for task ID: {}", createdTask.getId());
        log.debug("Response body - id: {}, status: {}, createdAt: {}", 
                  response.getId(), response.getStatus(), response.getCreatedAt());
        
        // Return HTTP 201 Created with Location header and response body
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/tasks/" + createdTask.getId())
                .body(response);
    }
}
