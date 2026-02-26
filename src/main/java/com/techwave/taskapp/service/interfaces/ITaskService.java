package com.techwave.taskapp.service.interfaces;

import com.techwave.taskapp.dto.CreateTaskRequest;
import com.techwave.taskapp.entity.Task;

/**
 * TaskService Interface - Contract for task business operations.
 * 
 * SOLID Principles:
 * - Dependency Inversion Principle (DIP): Depend on abstraction, not concretions
 * - Interface Segregation Principle (ISP): Focused interface for task operations
 * 
 * Benefits:
 * - Easier unit testing with mocks
 * - Supports multiple implementations
 * - Loose coupling between layers
 */
public interface ITaskService {
    
    /**
     * Create a new task from the provided request.
     * 
     * @param request CreateTaskRequest containing title and optional description
     * @return Created Task entity with generated ID and metadata
     * @throws IllegalArgumentException if request validation fails
     */
    Task createTask(CreateTaskRequest request);
}