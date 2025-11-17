package com.diagnet.gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * JWT Authentication Filter
 * 
 * PURPOSE:
 * Intercepts every request and validates JWT token
 * 
 * FLOW:
 * 1. Extract Authorization header from request
 * 2. Check if it starts with "Bearer "
 * 3. Extract token (remove "Bearer " prefix)
 * 4. Validate token using JwtUtil
 * 5. If valid, set authentication in SecurityContext
 * 6. Continue filter chain
 * 
 * WHY REACTIVE:
 * Spring Cloud Gateway is built on WebFlux (reactive)
 * So we use WebFilter instead of traditional Filter
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip authentication for public endpoints
        String path = request.getPath().value();
        if (isPublicPath(path)) {
            log.debug("Public path, skipping authentication: {}", path);
            return chain.filter(exchange);
        }
        
        // Extract token from Authorization header
        String token = extractToken(request);
        
        if (token != null && jwtUtil.validateToken(token)) {
            // Token is valid, extract username and set authentication
            String username = jwtUtil.getUsernameFromToken(token);
            log.debug("Valid token for user: {}", username);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
            
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        }
        
        log.debug("No valid token found for path: {}", path);
        return chain.filter(exchange);
    }
    
    /**
     * Extract JWT token from Authorization header
     * 
     * @param request HTTP request
     * @return Token string or null
     */
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        
        return null;
    }
    
    /**
     * Check if path is public (no authentication needed)
     * 
     * @param path Request path
     * @return true if public
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/auth/") || 
               path.startsWith("/actuator/");  // All actuator endpoints public
    }
}
