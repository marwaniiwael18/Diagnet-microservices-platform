# Collector Service Dockerfile
# Multi-stage build for smaller final image

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1000 diagnet && \
    adduser -u 1000 -G diagnet -s /bin/sh -D diagnet

# Copy JAR from builder stage
COPY --from=builder /app/target/collector-service-*.jar app.jar

# Change ownership
RUN chown -R diagnet:diagnet /app

# Switch to non-root user
USER diagnet

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
