package com.techwave.taskapp.enums;

/**
 * TaskStatus Enum - Represents the status of a task.
 * 
 * Currently only supports PENDING status for newly created tasks.
 * Can be extended in the future to include COMPLETED, IN_PROGRESS, etc.
 */
public enum TaskStatus {
    PENDING("PENDING"),
    IN_PROGRESS("IN_PROGRESS"),
    COMPLETED("COMPLETED");
    
    private final String value;
    
    TaskStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Get TaskStatus from string value (case-insensitive).
     * Defaults to PENDING if not found.
     */
    public static TaskStatus fromValue(String value) {
        if (value == null) {
            return PENDING;
        }
        for (TaskStatus status : TaskStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return PENDING;
    }
}
