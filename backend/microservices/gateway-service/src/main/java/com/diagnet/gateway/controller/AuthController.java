package com.diagnet.gateway.controller;

import com.diagnet.gateway.model.AuthResponse;
import com.diagnet.gateway.model.LoginRequest;
import com.diagnet.gateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * 
 * PURPOSE:
 * Handle user login and JWT token generation
 * 
 * ENDPOINTS:
 * - POST /auth/login - Login and get JWT token
 * - GET /auth/validate - Validate JWT token
 * 
 * NOTE: For simplicity, using hardcoded users
 * In production, use database with User entity
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    // Hardcoded users for demo (in production, use database)
    // Password: "admin123" hashed with BCrypt
    private static final Map<String, String> USERS = new HashMap<>();
    
    static {
        // admin / admin123
        USERS.put("admin", "$2a$10$xQ3jKYP2EP8RKmFKpzFKLeXRqLFPJlL3hI4XMRNz.XtZLHOLADLay");
        // user / user123
        USERS.put("user", "$2a$10$fFD8MpZLKpFKzMNZvdYXm.R0qQDqF8XwL9pT8w/N7eYgFZ0gkMJ1u");
    }
    
    /**
     * Login endpoint
     * 
     * EXAMPLE:
     * POST http://localhost:8080/auth/login
     * {
     *   "username": "admin",
     *   "password": "admin123"
     * }
     * 
     * RESPONSE:
     * {
     *   "token": "eyJhbGciOiJIUzUxMiJ9...",
     *   "type": "Bearer",
     *   "username": "admin",
     *   "expiresIn": 86400000
     * }
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        
        // Check if user exists
        String storedPassword = USERS.get(request.getUsername());
        if (storedPassword == null) {
            log.warn("User not found: {}", request.getUsername());
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), storedPassword)) {
            log.warn("Invalid password for user: {}", request.getUsername());
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(request.getUsername());
        
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(request.getUsername())
                .expiresIn(jwtUtil.getExpiration())
                .build();
        
        log.info("Login successful for user: {}", request.getUsername());
        return Mono.just(ResponseEntity.ok(response));
    }
    
    /**
     * Validate token endpoint
     * 
     * EXAMPLE:
     * GET http://localhost:8080/auth/validate
     * Headers: Authorization: Bearer <token>
     * 
     * RESPONSE:
     * {
     *   "valid": true,
     *   "username": "admin"
     * }
     */
    @GetMapping("/validate")
    public Mono<ResponseEntity<Map<String, Object>>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.ok(Map.of("valid", false)));
        }
        
        String token = authHeader.substring(7);
        boolean isValid = jwtUtil.validateToken(token);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        
        if (isValid) {
            String username = jwtUtil.getUsernameFromToken(token);
            response.put("username", username);
        }
        
        return Mono.just(ResponseEntity.ok(response));
    }
    
    /**
     * Health check for auth service
     * 
     * EXAMPLE: GET http://localhost:8080/auth/health
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "auth",
                "port", "8080"
        )));
    }
}
