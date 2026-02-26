package com.techwave.taskapp.service;

import com.techwave.taskapp.dto.CreateTaskRequest;
import com.techwave.taskapp.entity.Task;
import com.techwave.taskapp.repository.interfaces.ITaskRepository;
import com.techwave.taskapp.service.interfaces.ITaskService;
import com.techwave.taskapp.service.mapper.TaskMapper;
import com.techwave.taskapp.service.validation.TaskValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * TaskService - Business logic layer for task operations.
 * 
 * SOLID Principles Applied:
 * - Single Responsibility Principle (SRP): Orchestrates task creation, delegates specific concerns
 * - Open/Closed Principle (OCP): Extensible through dependency injection of collaborators
 * - Liskov Substitution Principle (LSP): Implements ITaskService contract
 * - Interface Segregation Principle (ISP): Depends on focused interfaces
 * - Dependency Inversion Principle (DIP): Depends on abstractions (interfaces)
 * 
 * Layered Architecture: Service Layer
 * - Orchestrates business operations
 * - No HTTP concerns (handled by controller)
 * - No validation logic (handled by TaskValidator)
 * - No mapping logic (handled by TaskMapper)
 */
@Slf4j
@Service
public class TaskService implements ITaskService {
    
    private final ITaskRepository taskRepository;
    private final TaskValidator taskValidator;
    private final TaskMapper taskMapper;
    
    /**
     * Constructor injection for all dependencies.
     * 
     * SOLID: Dependency Inversion Principle - depends on abstractions
     * Architecture: Constructor injection ensures immutability and testability
     * 
     * @param taskRepository Task data access interface
     * @param taskValidator Task validation component
     * @param taskMapper Task mapping component
     */
    public TaskService(ITaskRepository taskRepository, 
                      TaskValidator taskValidator, 
                      TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskValidator = taskValidator;
        this.taskMapper = taskMapper;
        
        log.info("TaskService initialized with dependencies - repository: {}, validator: {}, mapper: {}", 
                 taskRepository.getClass().getSimpleName(),
                 taskValidator.getClass().getSimpleName(),
                 taskMapper.getClass().getSimpleName());
    }
    
    /**
     * Create a new task based on the request.
     * 
     * SOLID: Single Responsibility - orchestrates task creation by delegating to specialists:
     * 1. TaskValidator: Handles business validation and sanitization
     * 2. TaskMapper: Handles DTO to Entity mapping
     * 3. TaskRepository: Handles data persistence
     * 
     * Business Flow:
     * 1. Validate request (delegate to validator)
     * 2. Sanitize input data (delegate to validator)
     * 3. Map DTO to Entity (delegate to mapper)
     * 4. Persist entity (delegate to repository)
     * 5. Log and return result
     * 
     * @param request CreateTaskRequest with title and optional description
     * @return Created Task entity with ID, status, and timestamp
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public Task createTask(CreateTaskRequest request) {
        Instant startTime = Instant.now();
        String requestId = generateRequestId();
        
        log.info("[{}] Starting task creation - title: '{}', description provided: {}", 
                 requestId, 
                 request.getTitle(), 
                 request.getDescription() != null);
        
        try {
            // Step 1: Validate request (SRP - delegate to specialist)
            taskValidator.validateCreateRequest(request);
            log.debug("[{}] Request validation completed", requestId);
            
            // Step 2: Sanitize input (SRP - delegate to specialist)
            taskValidator.sanitizeCreateRequest(request);
            log.debug("[{}] Request sanitization completed", requestId);
            
            // Step 3: Map DTO to Entity (SRP - delegate to specialist)
            Task task = taskMapper.mapToEntity(request);
            log.debug("[{}] Entity mapping completed - status: {}", requestId, task.getStatus());
            
            // Step 4: Persist entity (SRP - delegate to specialist)
            Task createdTask = taskRepository.create(task);
            
            // Step 5: Log success and return
            long durationMs = java.time.Duration.between(startTime, Instant.now()).toMillis();
            log.info("[{}] Task created successfully - ID: {}, title: '{}', duration: {}ms", 
                     requestId,
                     createdTask.getId(), 
                     createdTask.getTitle(),
                     durationMs);
            
            return createdTask;
            
        } catch (IllegalArgumentException e) {
            long durationMs = java.time.Duration.between(startTime, Instant.now()).toMillis();
            log.warn("[{}] Task creation failed due to validation error - title: '{}', error: {}, duration: {}ms", 
                     requestId, 
                     request.getTitle(), 
                     e.getMessage(),
                     durationMs);
            throw e;
            
        } catch (Exception e) {
            long durationMs = java.time.Duration.between(startTime, Instant.now()).toMillis();
            log.error("[{}] Task creation failed due to unexpected error - title: '{}', duration: {}ms", 
                      requestId, 
                      request.getTitle(),
                      durationMs, e);
            throw new RuntimeException("Failed to create task due to internal error", e);
        }
    }
    
    /**
     * Generate a unique request ID for logging correlation.
     * 
     * @return Short unique identifier for this request
     */
    private String generateRequestId() {
        return "req-" + System.nanoTime() % 1000000;
    }
}
