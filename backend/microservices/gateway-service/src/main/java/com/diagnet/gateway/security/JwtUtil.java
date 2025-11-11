package com.diagnet.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Utility Service
 * 
 * PURPOSE:
 * - Generate JWT tokens for authenticated users
 * - Validate JWT tokens from requests
 * - Extract username from tokens
 * 
 * HOW JWT WORKS:
 * 1. User logs in with username/password
 * 2. Server generates JWT token with user info + expiration
 * 3. Client stores token (localStorage/cookie)
 * 4. Client sends token with every request (Authorization header)
 * 5. Server validates token and allows/denies access
 * 
 * JWT STRUCTURE:
 * Header.Payload.Signature
 * - Header: token type + algorithm
 * - Payload: user data (username, expiration)
 * - Signature: ensures token wasn't tampered with
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Generate JWT token for a username
     * 
     * @param username User identifier
     * @return JWT token string
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
                
        log.debug("Generated JWT token for user: {}", username);
        return token;
    }
    
    /**
     * Extract username from JWT token
     * 
     * @param token JWT token
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
                
        return claims.getSubject();
    }
    
    /**
     * Validate JWT token
     * 
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if token is expired
     * 
     * @param token JWT token
     * @return true if expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
                    
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Get expiration time in milliseconds
     * 
     * @return Expiration time
     */
    public Long getExpiration() {
        return expiration;
    }
}
