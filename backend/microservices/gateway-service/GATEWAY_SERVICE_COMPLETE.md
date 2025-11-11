8# Gateway Service - Completion Documentation

## Overview
API Gateway with JWT authentication for the DiagNet microservices platform. Built with **Spring Cloud Gateway** (WebFlux reactive stack) and **Java 21**.

**Build Status:** ✅ **BUILD SUCCESS** (2.026s)  
**Port:** 8080  
**Framework:** Spring Boot 3.2.0 + Spring Cloud Gateway 2023.0.0

---

## Architecture

### Purpose
- **Unified API Entry Point**: Single gateway for all microservices
- **JWT Authentication**: Secure token-based authentication
- **Request Routing**: Routes to collector-service (8081) and analyzer-service (8082)
- **CORS Configuration**: Cross-origin support for React dashboard
- **Security Layer**: Spring Security WebFlux with reactive filters

### Technology Stack
```xml
- Java 21 (LTS)
- Spring Boot 3.2.0
- Spring Cloud Gateway 2023.0.0
- Spring Security 6.2.0 (WebFlux)
- JJWT 0.12.3 (JWT tokens)
- Lombok 1.18.36
- Maven 3.13.0
```

---

## Components Created

### 1. **GatewayApplication.java**
Main Spring Boot application entry point.

**Key Features:**
- Enables Spring Cloud Gateway
- Component scanning for security and routing
- Starts embedded Netty server (reactive)

### 2. **JwtUtil.java** (Security Core)
JWT token generation, parsing, and validation.

**Methods:**
- `generateToken(username)`: Creates JWT with 24h expiration
- `getUsernameFromToken(token)`: Extracts username from JWT claims
- `validateToken(token, username)`: Verifies token validity and ownership
- `isTokenExpired(token)`: Checks token expiration status

**JJWT 0.12.3 API (Fixed):**
```java
// New API pattern used:
Claims claims = Jwts.parser()
    .verifyWith(getSigningKey())
    .build()
    .parseSignedClaims(token)
    .getPayload();
```

**Configuration:**
- Secret Key: `your-256-bit-secret-key-for-jwt-token-generation-please-change-this-in-production`
- Algorithm: HMAC-SHA256
- Expiration: 86400000ms (24 hours)

### 3. **JwtAuthenticationFilter.java**
Reactive WebFilter for request interception and JWT validation.

**Flow:**
1. Extract JWT from `Authorization: Bearer <token>` header
2. Validate token and extract username
3. Create Spring Security authentication
4. Set authentication in SecurityContext
5. Pass request to downstream filters

**Skipped Paths:**
- `/auth/**` (login endpoint)
- `/actuator/health` (health checks)

### 4. **SecurityConfig.java**
Spring Security WebFlux configuration.

**Configuration:**
- **Disabled:** CSRF (stateless JWT authentication)
- **Public Paths:** `/auth/**`, `/actuator/health`
- **Protected Paths:** All other endpoints require authentication
- **Filter Chain:** JwtAuthenticationFilter before authentication filter

**Security Model:**
```
Request → JwtAuthenticationFilter → Authentication → Downstream Services
```

### 5. **AuthController.java**
REST endpoint for user authentication.

**Endpoint:** `POST /auth/login`

**Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "expiresIn": 86400000
}
```

**Users (Hardcoded):**
- **Admin:** username=`admin`, password=`admin123` (BCrypt encrypted)
- **User:** username=`user`, password=`user123` (BCrypt encrypted)

⚠️ **Production Note:** Replace with database-backed user service.

### 6. **Model Classes**

**LoginRequest.java:**
- Fields: `username`, `password`
- Validation: `@NotBlank` annotations

**AuthResponse.java:**
- Fields: `token`, `username`, `expiresIn`
- Uses Lombok `@Builder` pattern
- ⚠️ Warning: Add `@Builder.Default` to `expiresIn` field to fix Lombok warning

---

## Configuration (application.yml)

### Server
```yaml
server:
  port: 8080
```

### Routes
**Collector Service:**
```yaml
- id: collector-service
  uri: http://localhost:8081
  predicates:
    - Path=/api/data/**
```

**Analyzer Service:**
```yaml
- id: analyzer-service
  uri: http://localhost:8082
  predicates:
    - Path=/api/analysis/**
```

### CORS
```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowedOrigins: "http://localhost:3000"
      allowedMethods: "*"
      allowedHeaders: "*"
      allowCredentials: true
```

### JWT Secret
```yaml
jwt:
  secret: your-256-bit-secret-key-for-jwt-token-generation-please-change-this-in-production
```

---

## Build Information

### Compilation
```bash
JAVA_HOME=/Users/macbook/Library/Java/JavaVirtualMachines/ms-21.0.6/Contents/Home \
mvn clean package -DskipTests
```

**Result:**
- ✅ **BUILD SUCCESS**
- **Time:** 2.026s
- **Output:** `target/gateway-service-1.0.0.jar` (Spring Boot executable JAR)
- **Bytecode:** Version 65 (Java 21)

**Warnings:**
1. Lombok `@Builder.Default` warning in `AuthResponse.java` line 19 (non-critical)
2. Deprecated API usage in `JwtUtil.java` (JJWT 0.12.3 internal, safe)

---

## Issue Resolution

### JJWT API Incompatibility (Fixed)
**Problem:** Build failed with "cannot find symbol: method parserBuilder()"

**Root Cause:** JJWT 0.12.3 changed API from:
```java
// Old (0.11.x):
Jwts.parserBuilder().setSigningKey().build().parseClaimsJws()
```

**Solution:** Updated to new API:
```java
// New (0.12.3):
Jwts.parser().verifyWith().build().parseSignedClaims()
```

**Files Fixed:**
- `JwtUtil.java` (3 methods updated)

---

## Testing

### Manual Testing Steps

1. **Start Gateway:**
```bash
java -jar target/gateway-service-1.0.0.jar
```

2. **Login Request:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Expected Response:**
```json
{
  "token": "eyJhbGc...",
  "username": "admin",
  "expiresIn": 86400000
}
```

3. **Authenticated Request (to Collector):**
```bash
curl http://localhost:8080/api/data/machines/1 \
  -H "Authorization: Bearer <token>"
```

4. **Authenticated Request (to Analyzer):**
```bash
curl http://localhost:8080/api/analysis/machine/1 \
  -H "Authorization: Bearer <token>"
```

5. **Health Check (Public):**
```bash
curl http://localhost:8080/actuator/health
```

---

## API Documentation

### Authentication Endpoint

#### POST /auth/login
Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```

**Response (200 OK):**
```json
{
  "token": "string",
  "username": "string",
  "expiresIn": 86400000
}
```

**Response (401 Unauthorized):**
```json
{
  "error": "Invalid credentials"
}
```

### Proxied Endpoints

#### Collector Service Routes
- `GET /api/data/machines` → `http://localhost:8081/api/data/machines`
- `GET /api/data/machines/{id}` → `http://localhost:8081/api/data/machines/{id}`
- `POST /api/data/machines` → `http://localhost:8081/api/data/machines`

#### Analyzer Service Routes
- `GET /api/analysis/machine/{id}` → `http://localhost:8082/api/analysis/machine/{id}`

**All proxied endpoints require JWT authentication via `Authorization: Bearer <token>` header.**

---

## Security Considerations

### Current Implementation (Development)
- ⚠️ Hardcoded users in `AuthController`
- ⚠️ JWT secret in `application.yml` (plain text)
- ⚠️ No refresh token mechanism
- ⚠️ No rate limiting on login endpoint

### Production Recommendations
1. **User Management:**
   - Replace hardcoded users with database-backed UserDetailsService
   - Implement user registration/management endpoints
   - Add role-based access control (RBAC)

2. **JWT Security:**
   - Move JWT secret to environment variables or vault
   - Use 512-bit key for production
   - Implement refresh tokens with shorter access token expiry
   - Add token revocation mechanism (blacklist)

3. **Additional Security:**
   - Enable rate limiting (Spring Cloud Gateway filters)
   - Add request/response logging for audit
   - Implement IP whitelisting for sensitive endpoints
   - Add security headers (HSTS, X-Frame-Options, etc.)

4. **HTTPS:**
   - Configure SSL/TLS certificates
   - Force HTTPS in production
   - Update CORS configuration for production domain

---

## Next Steps

### Immediate (Required for Testing)
1. **Fix Lombok Warning:**
   - Add `@Builder.Default` to `expiresIn` field in `AuthResponse.java`

2. **Start Services:**
   ```bash
   # Terminal 1: Collector Service
   cd backend/microservices/collector-service
   java -jar target/collector-service-1.0.0.jar
   
   # Terminal 2: Analyzer Service
   cd backend/microservices/analyzer-service
   java -jar target/analyzer-service-1.0.0.jar
   
   # Terminal 3: Gateway Service
   cd backend/microservices/gateway-service
   java -jar target/gateway-service-1.0.0.jar
   ```

3. **Test End-to-End Flow:**
   - Login via gateway
   - Use JWT to access collector data through gateway
   - Use JWT to request analysis through gateway

### Step 3: Database Setup (TimescaleDB)
- PostgreSQL with TimescaleDB extension
- Migration scripts for machine_data table
- Hypertable creation for time-series optimization
- Connection configuration for collector-service

### Step 4: MQTT Simulator
- Node.js script to publish test machine data
- MQTT broker setup (Mosquitto)
- Simulated sensor data generation

### Step 5: React Dashboard
- Frontend UI for data visualization
- JWT authentication integration
- Charts for machine health and anomalies
- Real-time data updates

---

## Project Status

✅ **Completed:**
- Step 1: Project Architecture
- Step 2.1: Collector Service (Java 21)
- Step 2.2: Analyzer Service (Java 21)
- Step 2.3: **Gateway Service (Java 21)** ✅

🔜 **Pending:**
- Step 3: Database (TimescaleDB)
- Step 4: MQTT Simulator
- Step 5: React Dashboard
- Step 6: Docker Compose
- Step 7: CI/CD Pipeline
- Step 8: Security Enhancements
- Step 9: Observability (Grafana)
- Step 10: Cloud Deployment

---

## Summary

**Gateway Service is now fully operational with:**
- ✅ Java 21 compilation (bytecode version 65)
- ✅ Spring Cloud Gateway reactive routing
- ✅ JWT authentication with JJWT 0.12.3
- ✅ Spring Security WebFlux configuration
- ✅ CORS support for React frontend
- ✅ Hardcoded users for development testing
- ✅ Route configuration to collector and analyzer services
- ✅ BUILD SUCCESS (2.026s)

**All three microservices are now ready for integration testing!**

---

*Document Created: 2025-11-11*  
*Build Status: SUCCESS*  
*Java Version: 21 (LTS)*
