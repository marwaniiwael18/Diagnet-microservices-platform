package com.diagnet.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

/**
 * Security Configuration
 * 
 * PURPOSE:
 * - Configure security rules for Gateway
 * - Define which paths require authentication
 * - Add JWT filter to validate tokens
 * - Configure CORS for React frontend
 * 
 * WHY @EnableWebFluxSecurity:
 * Gateway uses Spring WebFlux (reactive), not traditional Spring MVC
 * So we need WebFlux security configuration
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // ENABLE CORS - use the CorsWebFilter from CorsConfig
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
                    config.setAllowedHeaders(Arrays.asList("*"));
                    config.setExposedHeaders(Arrays.asList("Authorization"));
                    config.setAllowCredentials(true);
                    config.setMaxAge(3600L);
                    return config;
                }))
                
                // Disable CSRF (not needed for stateless JWT API)
                .csrf(csrf -> csrf.disable())
                
                // Configure authorization rules
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints (no auth needed)
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()  // All actuator endpoints
                        
                        // All other endpoints require authentication
                        .anyExchange().authenticated()
                )
                
                // Add JWT filter before authentication
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                
                .build();
    }
    
    /**
     * Password encoder for hashing passwords
     * 
     * WHY BCrypt:
     * - Industry standard
     * - Automatically salted
     * - Configurable strength (default: 10 rounds)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
