package com.techwave.taskapp.repository;

import com.techwave.taskapp.entity.Task;
import com.techwave.taskapp.repository.interfaces.ITaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TaskRepository - In-memory data access layer for tasks.
 * 
 * Layered Architecture: Repository Layer (Data Persistence)
 * Uses ConcurrentHashMap for thread-safe storage (per database developer instructions).
 * 
 * Features:
 * - Thread-safe concurrent operations
 * - UUID-based task identification
 * - No external database dependency (in-memory only)
 * - Data is volatile and lost on application restart
 */
@Slf4j
@Repository
public class TaskRepository implements ITaskRepository {
    
    /**
     * In-memory storage using ConcurrentHashMap for thread safety.
     * Key: Task ID (UUID string)
     * Value: Task entity
     */
    private final ConcurrentHashMap<String, Task> taskStore = new ConcurrentHashMap<>();
    
    /**
     * Retrieve a task by ID.
     * 
     * @param id Task identifier
     * @return Optional containing the task if found, empty otherwise
     */
    public Optional<Task> findById(String id) {
        log.debug("Retrieving task with id: {}", id);
        return Optional.ofNullable(taskStore.get(id));
    }
    
    /**
     * Create and store a new task.
     * 
     * Handles:
     * - UUID generation for task ID
     * - Storage in ConcurrentHashMap (thread-safe)
     * - Logging of creation
     * 
     * @param task Task entity to be created (without ID)
     * @return Created task with generated ID
     */
    public Task create(Task task) {
        // Generate unique UUID for the task
        String taskId = UUID.randomUUID().toString();
        task.setId(taskId);
        
        // Store in thread-safe map
        taskStore.put(taskId, task);
        
        log.info("Task created successfully with ID: {}, title: {}", taskId, task.getTitle());
        log.debug("Task details - status: {}, createdAt: {}", task.getStatus(), task.getCreatedAt());
        
        return task;
    }
    
    /**
     * Get total count of tasks in storage.
     * Useful for testing and monitoring.
     * 
     * @return Number of tasks currently stored
     */
    public int count() {
        return taskStore.size();
    }
    
    /**
     * Clear all tasks from storage.
     * Useful for testing scenarios.
     */
    public void clear() {
        log.warn("Clearing all tasks from storage");
        taskStore.clear();
    }
}
