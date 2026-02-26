package com.techwave.taskapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GlobalExceptionHandler - Centralized exception handling for the Task API.
 * 
 * Handles:
 * - Bean validation errors (@Valid failures)
 * - Business logic exceptions
 * - Unexpected server errors
 * 
 * Returns FRD-compliant error responses with timestamps and error codes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle Bean Validation errors (e.g., @NotBlank, @Size violations).
     * 
     * Returns HTTP 400 with detailed field-level error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String requestId = generateRequestId();
        
        log.warn("[{}] Validation error occurred: {}", requestId, ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("code", "VALIDATION_FAILED");
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("request_id", requestId);
        
        // Extract field-specific errors
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String field = error.getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
            
            log.debug("[{}] Field error - {}: {}", requestId, field, message);
        }
        
        if (!fieldErrors.isEmpty()) {
            errorResponse.put("errors", fieldErrors);
            
            // Set primary message based on first field error
            FieldError firstError = ex.getBindingResult().getFieldErrors().get(0);
            if ("title".equals(firstError.getField())) {
                if (firstError.getCode() != null && firstError.getCode().contains("NotBlank")) {
                    errorResponse.put("code", "MISSING_TITLE");
                    errorResponse.put("message", "Title field is required");
                } else if (firstError.getCode() != null && firstError.getCode().contains("Size")) {
                    errorResponse.put("code", "TITLE_TOO_LONG");
                    errorResponse.put("message", "Title cannot exceed 255 characters");
                } else {
                    errorResponse.put("message", firstError.getDefaultMessage());
                }
            } else if ("description".equals(firstError.getField())) {
                errorResponse.put("code", "DESCRIPTION_TOO_LONG");
                errorResponse.put("message", "Description cannot exceed 2000 characters");
            } else {
                errorResponse.put("message", firstError.getDefaultMessage());
            }
        } else {
            errorResponse.put("message", "Invalid request data");
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle IllegalArgumentException (business logic validation errors).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        String requestId = generateRequestId();
        
        log.warn("[{}] Business validation error: {}", requestId, ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("code", "INVALID_TITLE");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("request_id", requestId);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle unexpected server errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        String requestId = generateRequestId();
        
        log.error("[{}] Unexpected server error occurred", requestId, ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("code", "SERVER_ERROR");
        errorResponse.put("message", "An unexpected error occurred while creating the task");
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("request_id", requestId);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Generate a unique request ID for error tracking.
     */
    private String generateRequestId() {
        return "req-" + UUID.randomUUID().toString().substring(0, 8);
    }
}