package com.techwave.taskapp.service.validation;

import com.techwave.taskapp.dto.CreateTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * TaskValidator - Handles business validation logic for task operations.
 * 
 * SOLID Principles:
 * - Single Responsibility Principle (SRP): Only responsible for validation logic
 * - Open/Closed Principle (OCP): Easy to extend with new validation rules
 * 
 * Separation of Concerns:
 * - Bean validation (@NotBlank, @Size) handles basic constraints
 * - This validator handles business-specific validation and data sanitization
 */
@Slf4j
@Component
public class TaskValidator {
    
    /**
     * Validate and sanitize the create task request.
     * 
     * Business Rules:
     * - Title must be non-empty after trimming (beyond @NotBlank)
     * - Description empty strings converted to null for consistency
     * - Whitespace handling per FRD requirements
     * 
     * @param request CreateTaskRequest to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateCreateRequest(CreateTaskRequest request) {
        log.debug("Validating create task request - title: '{}', description length: {}", 
                  request.getTitle(), 
                  request.getDescription() != null ? request.getDescription().length() : 0);
        
        // Additional title validation beyond @NotBlank
        if (!StringUtils.hasText(request.getTitle())) {
            log.warn("Title validation failed - empty or whitespace only: '{}'", request.getTitle());
            throw new IllegalArgumentException("Title cannot be empty or contain only whitespace");
        }
        
        log.debug("Task request validation completed successfully");
    }
    
    /**
     * Sanitize and clean input data according to business rules.
     * 
     * Business Rules:
     * - Trim leading/trailing whitespace from title and description
     * - Convert empty description to null for consistency
     * - Preserve internal whitespace
     * 
     * @param request CreateTaskRequest to sanitize (modified in place)
     */
    public void sanitizeCreateRequest(CreateTaskRequest request) {
        log.debug("Sanitizing create task request");
        
        // Trim title (required field)
        if (request.getTitle() != null) {
            String originalTitle = request.getTitle();
            String trimmedTitle = request.getTitle().trim();
            request.setTitle(trimmedTitle);
            
            if (!originalTitle.equals(trimmedTitle)) {
                log.debug("Title trimmed - original: '{}', trimmed: '{}'", originalTitle, trimmedTitle);
            }
        }
        
        // Trim and normalize description (optional field)
        if (request.getDescription() != null) {
            String originalDescription = request.getDescription();
            String trimmedDescription = request.getDescription().trim();
            
            // Convert empty string to null for consistent response format
            if (trimmedDescription.isEmpty()) {
                request.setDescription(null);
                log.debug("Description converted from empty string to null");
            } else {
                request.setDescription(trimmedDescription);
                if (!originalDescription.equals(trimmedDescription)) {
                    log.debug("Description trimmed - original length: {}, trimmed length: {}", 
                              originalDescription.length(), trimmedDescription.length());
                }
            }
        }
        
        log.debug("Request sanitization completed");
    }
}