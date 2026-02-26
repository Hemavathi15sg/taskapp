package com.techwave.taskapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * TaskApp Application - Spring Boot entry point for Techwave Task Tracker.
 * 
 * Features:
 * - Task creation API (POST /api/tasks)
 * - In-memory storage with ConcurrentHashMap
 * - Layered architecture with validation
 * - CORS support for web UI
 * - Health endpoints for monitoring
 */
@Slf4j
@SpringBootApplication
public class TaskAppApplication {

    public static void main(String[] args) {
        log.info("Starting Techwave Task Tracker Application...");
        
        ConfigurableApplicationContext context = SpringApplication.run(TaskAppApplication.class, args);
        
        String serverPort = context.getEnvironment().getProperty("server.port", "8080");
        log.info("🚀 Task Tracker Application started successfully!");
        log.info("📡 API available at: http://localhost:{}/api/tasks", serverPort);
        log.info("🌐 Web UI available at: http://localhost:{}", serverPort);
        log.info("❤️ Health check: http://localhost:{}/actuator/health", serverPort);
        log.info("📊 Actuator endpoints: http://localhost:{}/actuator", serverPort);
    }
}