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
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

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
                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Disable CSRF (not needed for stateless JWT API)
                .csrf(csrf -> csrf.disable())
                
                // Configure authorization rules
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints (no auth needed)
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers("/actuator/info").permitAll()
                        
                        // All other endpoints require authentication
                        .anyExchange().authenticated()
                )
                
                // Add JWT filter before authentication
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                
                .build();
    }
    
    /**
     * CORS Configuration for React Frontend
     * 
     * WHY CORS:
     * - Frontend runs on http://localhost:5173
     * - Backend runs on http://localhost:8080
     * - Browser blocks cross-origin requests by default
     * - CORS allows React to call our API
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",  // React dev server
            "http://localhost:3000"   // Alternative React port
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
