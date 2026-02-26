package com.techwave.taskapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.techwave.taskapp.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * TaskResponse DTO - Represents the response payload when a task is retrieved or created.
 * 
 * Layered Architecture: DTO Layer (Controller output)
 * Follows FRD response format specifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    
    /**
     * Unique identifier for the task (UUID format).
     * Auto-generated on creation.
     */
    private String id;
    
    /**
     * Task title.
     */
    private String title;
    
    /**
     * Task description - can be null if not provided.
     */
    private String description;
    
    /**
     * Task status (e.g., PENDING, IN_PROGRESS, COMPLETED).
     * Defaults to PENDING on creation.
     */
    private String status;
    
    /**
     * Timestamp when the task was created (UTC, ISO 8601 format).
     * Format: 2026-02-26T12:00:15.246Z
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
}
