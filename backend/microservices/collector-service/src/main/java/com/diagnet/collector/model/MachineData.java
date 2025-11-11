package com.diagnet.collector.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity class representing machine sensor data
 * 
 * WHY THIS CLASS EXISTS:
 * - Represents a single row in the machine_data table
 * - JPA (Java Persistence API) maps this class to database table
 * - Each instance = one sensor reading from a machine
 * 
 * ANNOTATIONS EXPLAINED:
 * - @Entity: Tells JPA this is a database table
 * - @Table: Specifies the table name (optional, defaults to class name)
 * - @Data (Lombok): Auto-generates getters, setters, toString, equals, hashCode
 * - @NoArgsConstructor (Lombok): Creates empty constructor (required by JPA)
 * - @AllArgsConstructor (Lombok): Creates constructor with all fields
 * 
 * EXAMPLE DATA:
 * {
 *   "id": 1,
 *   "machineId": "M001",
 *   "timestamp": "2025-11-11T10:30:00",
 *   "temperature": 75.5,
 *   "vibration": 0.3,
 *   "pressure": 101.3,
 *   "status": "running"
 * }
 */
@Entity
@Table(name = "machine_data", indexes = {
    // Index on timestamp for fast time-series queries
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    // Index on machineId for filtering by specific machine
    @Index(name = "idx_machine_id", columnList = "machineId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineData {

    /**
     * Primary key - auto-generated ID
     * @GeneratedValue: Database automatically assigns next ID
     * @Id: Marks this as the primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Machine identifier (e.g., "M001", "M002")
     * @Column: Specifies column details
     * - nullable = false: Cannot be null (required field)
     * - length = 50: Maximum 50 characters
     */
    @Column(nullable = false, length = 50)
    private String machineId;

    /**
     * When the reading was taken
     * This is crucial for time-series analysis
     * Indexed for fast queries like "show last hour of data"
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Temperature in Celsius
     * Normal range: 20-100°C
     * Alert if > 100°C
     */
    @Column(nullable = false)
    private Double temperature;

    /**
     * Vibration level (0.0 - 1.0)
     * 0.0 = no vibration
     * 1.0 = maximum vibration
     * Alert if > 0.8
     */
    @Column(nullable = false)
    private Double vibration;

    /**
     * Pressure in bar (optional)
     */
    @Column(nullable = true)
    private Double pressure;

    /**
     * Humidity percentage (0-100)
     */
    @Column(nullable = true)
    private Double humidity;

    /**
     * Power consumption in watts
     */
    @Column(name = "power_consumption", nullable = true)
    private Double powerConsumption;

    /**
     * Rotation speed in RPM
     */
    @Column(name = "rotation_speed", nullable = true)
    private Double rotationSpeed;

    /**
     * Physical location of the machine
     */
    @Column(nullable = true, length = 100)
    private String location;

    /**
     * Machine status: "RUNNING", "IDLE", "WARNING", "CRITICAL"
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * When this record was inserted into database
     * @PrePersist: Automatically set before saving to database
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Automatically set timestamp before saving
     * This is a JPA lifecycle callback
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
