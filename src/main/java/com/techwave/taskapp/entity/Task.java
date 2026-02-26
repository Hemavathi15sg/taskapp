package com.techwave.taskapp.entity;

import com.techwave.taskapp.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Task Entity - Represents a task in the system.
 * 
 * Layered Architecture: Data Model Layer
 * Follows Spring Boot best practices with Lombok annotations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    /**
     * Unique identifier for the task (UUID format).
     */
    private String id;
    
    /**
     * Task title - mandatory field.
     * Max 255 characters, must be non-empty after trimming.
     */
    private String title;
    
    /**
     * Task description - optional field.
     * Max 2000 characters, can be null or empty.
     */
    private String description;
    
    /**
     * Task status - defaults to PENDING on creation.
     * Stored as enum for type safety.
     */
    private TaskStatus status;
    
    /**
     * Timestamp when the task was created (UTC, ISO 8601 format).
     */
    private Instant createdAt;
}
