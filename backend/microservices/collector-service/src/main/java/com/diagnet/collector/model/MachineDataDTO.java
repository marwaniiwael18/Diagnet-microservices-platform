package com.diagnet.collector.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) for receiving machine data via REST API
 * 
 * WHY WE USE DTOs (instead of using Entity directly):
 * 1. Separation: API structure can differ from database structure
 * 2. Validation: Can add validation rules specific to API input
 * 3. Security: Don't expose internal database fields (like id, createdAt)
 * 4. Flexibility: API can evolve without changing database
 * 
 * VALIDATION ANNOTATIONS:
 * - @NotNull: Field is required
 * - @NotBlank: String must have content (not just whitespace)
 * - @Size: String length constraints
 * - @DecimalMin/@DecimalMax: Number range validation
 * - @Pattern: Must match regex pattern
 * 
 * EXAMPLE JSON REQUEST:
 * POST /api/data
 * {
 *   "machineId": "M001",
 *   "timestamp": "2025-11-11T10:30:00",
 *   "temperature": 75.5,
 *   "vibration": 0.3,
 *   "pressure": 101.3,
 *   "status": "running"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineDataDTO {

    /**
     * Machine ID validation:
     * - Cannot be null or empty
     * - Length between 1 and 50 characters
     */
    @NotBlank(message = "Machine ID is required")
    @Size(min = 1, max = 50, message = "Machine ID must be between 1 and 50 characters")
    private String machineId;

    /**
     * Timestamp validation:
     * - Cannot be null
     * - Must be in past (can't receive future data)
     * - Format: ISO 8601 (2025-11-11T10:30:00)
     */
    @NotNull(message = "Timestamp is required")
    @PastOrPresent(message = "Timestamp cannot be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Temperature validation (Celsius):
     * - Cannot be null
     * - Range: -50°C to 200°C (realistic industrial range)
     * - Alerts typically trigger at 100°C+
     */
    @NotNull(message = "Temperature is required")
    @DecimalMin(value = "-50.0", message = "Temperature must be at least -50°C")
    @DecimalMax(value = "200.0", message = "Temperature must not exceed 200°C")
    private Double temperature;

    /**
     * Vibration validation:
     * - Cannot be null
     * - Range: 0.0 (no vibration) to 1.0 (maximum)
     * - Alerts typically trigger at 0.8+
     */
    @NotNull(message = "Vibration level is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Vibration must be at least 0.0")
    @DecimalMax(value = "1.0", inclusive = true, message = "Vibration must not exceed 1.0")
    private Double vibration;

    /**
     * Pressure (optional bar):
     * - Can be null (not all machines report pressure)
     * - If provided: 0 to 10 bar
     */
    @DecimalMin(value = "0.0", message = "Pressure must be positive")
    @DecimalMax(value = "10.0", message = "Pressure must not exceed 10 bar")
    private Double pressure;

    /**
     * Humidity (optional %):
     * - Can be null
     * - If provided: 0 to 100%
     */
    @DecimalMin(value = "0.0", message = "Humidity must be positive")
    @DecimalMax(value = "100.0", message = "Humidity must not exceed 100%")
    private Double humidity;

    /**
     * Power consumption (watts):
     * - Can be null
     * - If provided: 0 to 10000 watts
     */
    @DecimalMin(value = "0.0", message = "Power consumption must be positive")
    @DecimalMax(value = "10000.0", message = "Power consumption must not exceed 10000 watts")
    private Double powerConsumption;

    /**
     * Rotation speed (RPM):
     * - Can be null
     * - If provided: 0 to 5000 RPM
     */
    @DecimalMin(value = "0.0", message = "Rotation speed must be positive")
    @DecimalMax(value = "5000.0", message = "Rotation speed must not exceed 5000 RPM")
    private Double rotationSpeed;

    /**
     * Machine location (optional):
     * - Can be null
     */
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    /**
     * Status validation:
     * - Cannot be null or empty
     * - Must be one of: RUNNING, IDLE, WARNING, CRITICAL
     */
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(RUNNING|IDLE|WARNING|CRITICAL)$", 
             message = "Status must be one of: RUNNING, IDLE, WARNING, CRITICAL")
    private String status;

    /**
     * Convert DTO to Entity
     * This method transforms API input to database entity
     * 
     * WHY: Keeps conversion logic in one place
     * 
     * @return MachineData entity ready to save to database
     */
    public MachineData toEntity() {
        MachineData entity = new MachineData();
        entity.setMachineId(this.machineId);
        entity.setTimestamp(this.timestamp);
        entity.setTemperature(this.temperature);
        entity.setVibration(this.vibration);
        entity.setPressure(this.pressure);
        entity.setHumidity(this.humidity);
        entity.setPowerConsumption(this.powerConsumption);
        entity.setRotationSpeed(this.rotationSpeed);
        entity.setLocation(this.location);
        entity.setStatus(this.status);
        return entity;
    }
}
