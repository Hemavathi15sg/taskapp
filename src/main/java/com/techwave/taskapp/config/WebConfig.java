package com.techwave.taskapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig - Web-related configuration for the Task Tracker application.
 * 
 * Features:
 * - CORS configuration for frontend integration
 * - Static resource handling
 * - Request/response customization
 */
@Slf4j
@Configuration
public class WebConfig {
    
    /**
     * CORS configuration to allow frontend integration.
     * 
     * Allows:
     * - Local development (localhost:8080, 127.0.0.1:8080)
     * - File protocol for static HTML testing
     * - All HTTP methods for API operations
     * - Credentials for authentication (future use)
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                log.info("Configuring CORS for Task API endpoints");
                
                registry.addMapping("/api/**")
                    .allowedOrigins(
                        "http://localhost:8080",
                        "http://127.0.0.1:8080", 
                        "file://",
                        "null" // For file:// protocol
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600); // 1 hour preflight cache
                
                log.debug("CORS configuration applied - origins: localhost:8080, 127.0.0.1:8080, file://");
            }
        };
    }
}