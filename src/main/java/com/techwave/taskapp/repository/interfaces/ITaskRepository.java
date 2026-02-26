package com.techwave.taskapp.repository.interfaces;

import com.techwave.taskapp.entity.Task;

import java.util.Optional;

/**
 * TaskRepository Interface - Contract for task data access operations.
 * 
 * SOLID Principles:
 * - Dependency Inversion Principle (DIP): Service layer depends on this abstraction
 * - Interface Segregation Principle (ISP): Focused interface for data operations
 * 
 * Benefits:
 * - Can swap between in-memory, database, or external service implementations
 * - Easier unit testing with repository mocks
 * - Clear separation of data access concerns
 */
public interface ITaskRepository {
    
    /**
     * Create and persist a new task.
     * 
     * @param task Task entity to be persisted (ID will be generated)
     * @return Persisted task with generated ID
     */
    Task create(Task task);
    
    /**
     * Retrieve a task by its unique identifier.
     * 
     * @param id Task identifier
     * @return Optional containing the task if found, empty otherwise
     */
    Optional<Task> findById(String id);
    
    /**
     * Get the total count of tasks in storage.
     * 
     * @return Number of tasks currently stored
     */
    int count();
    
    /**
     * Clear all tasks from storage.
     * Useful for testing scenarios.
     */
    void clear();
}