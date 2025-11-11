package com.diagnet.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Gateway Service - Main Application
 * 
 * PURPOSE:
 * - Unified entry point for all backend services
 * - Routes requests to collector-service and analyzer-service
 * - Handles authentication with JWT
 * - Provides CORS support for frontend
 * 
 * RUNS ON: Port 8080
 * 
 * ROUTES:
 * /api/data/**     → collector-service (8081)
 * /api/analysis/** → analyzer-service (8082)
 * /auth/**         → authentication (this service)
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
