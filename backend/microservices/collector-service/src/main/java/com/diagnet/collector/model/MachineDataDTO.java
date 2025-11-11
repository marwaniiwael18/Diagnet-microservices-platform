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
     * - Pattern: Must start with letter, followed by letters/numbers
     */
    @NotBlank(message = "Machine ID is required")
    @Size(min = 1, max = 50, message = "Machine ID must be between 1 and 50 characters")
    @Pattern(regexp = "^[A-Z][A-Z0-9]*$", 
             message = "Machine ID must start with a letter and contain only uppercase letters and numbers")
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
     * Pressure (optional):
     * - Can be null (not all machines report pressure)
     * - If provided: 0 to 500 kPa
     */
    @DecimalMin(value = "0.0", message = "Pressure must be positive")
    @DecimalMax(value = "500.0", message = "Pressure must not exceed 500 kPa")
    private Double pressure;

    /**
     * Status validation:
     * - Cannot be null or empty
     * - Must be one of: running, idle, error, maintenance
     */
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(running|idle|error|maintenance)$", 
             message = "Status must be one of: running, idle, error, maintenance")
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
        entity.setStatus(this.status);
        return entity;
    }
}
