package com.diagnet.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Auth Response DTO
 * 
 * WHY: Response after successful login containing JWT token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private Long expiresIn;  // milliseconds
}
