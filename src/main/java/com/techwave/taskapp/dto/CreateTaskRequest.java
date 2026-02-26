package com.techwave.taskapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CreateTaskRequest DTO - Represents the request payload for creating a task.
 * 
 * Layered Architecture: DTO Layer (Controller input)
 * Uses Spring validation annotations to enforce FRD requirements.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    
    /**
     * Task title - mandatory field.
     * 
     * Validation Rules (per FRD):
     * - Required: Cannot be null or empty
     * - Trimming: Leading/trailing whitespace ignored
     * - Max Length: 255 characters
     */
    @NotBlank(message = "Title is required and cannot be empty or whitespace only")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;
    
    /**
     * Task description - optional field.
     * 
     * Validation Rules (per FRD):
     * - Optional: Can be omitted from request
     * - Max Length: 2000 characters
     * - Null Handling: Empty string treated as null in response
     */
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
}
