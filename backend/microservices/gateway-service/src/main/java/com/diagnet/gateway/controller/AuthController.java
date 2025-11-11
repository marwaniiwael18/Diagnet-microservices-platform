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
@Slf4j
public class AuthController {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    // Hardcoded users for demo (in production, use database)
    private static final Map<String, String> USERS = new HashMap<>();
    
    // Constructor to initialize users with properly encoded passwords
    public AuthController(JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        
        // Generate and store password hashes on startup
        if (USERS.isEmpty()) {
            String adminHash = passwordEncoder.encode("admin123");
            String userHash = passwordEncoder.encode("user123");
            
            USERS.put("admin", adminHash);
            USERS.put("user", userHash);
            
            log.info("🔑 [INIT] Generated password hash for admin: {}", adminHash);
            log.info("🔑 [INIT] Generated password hash for user: {}", userHash);
        }
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
        log.info("🔐 [AUTH] Login attempt for user: {}", request.getUsername());
        log.debug("📝 [AUTH] Request details: username={}", request.getUsername());
        
        // Check if user exists
        String storedPassword = USERS.get(request.getUsername());
        if (storedPassword == null) {
            log.warn("❌ [AUTH] User not found: {}", request.getUsername());
            log.warn("📋 [AUTH] Available users: {}", USERS.keySet());
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        log.debug("✓ [AUTH] User exists: {}", request.getUsername());
        
        // Verify password
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), storedPassword);
        log.debug("🔑 [AUTH] Password match result: {}", passwordMatches);
        
        if (!passwordMatches) {
            log.warn("❌ [AUTH] Invalid password for user: {}", request.getUsername());
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        log.info("✓ [AUTH] Password verified for user: {}", request.getUsername());
        
        // Generate JWT token
        String token = jwtUtil.generateToken(request.getUsername());
        log.debug("🎫 [AUTH] Token generated (last 10 chars): ...{}", 
                 token.substring(Math.max(0, token.length() - 10)));
        
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(request.getUsername())
                .expiresIn(jwtUtil.getExpiration())
                .build();
        
        log.info("✅ [AUTH] Login successful for user: {}", request.getUsername());
        log.debug("📤 [AUTH] Sending response with token and username");
        
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
