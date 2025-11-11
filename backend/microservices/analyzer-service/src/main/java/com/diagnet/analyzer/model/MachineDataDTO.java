package com.diagnet.analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO representing machine data received from collector-service
 * 
 * WHY: We need to fetch data from collector-service to analyze it
 * This matches the MachineData entity in collector-service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineDataDTO {
    private Long id;
    private String machineId;
    private LocalDateTime timestamp;
    private Double temperature;
    private Double vibration;
    private Double pressure;
    private String status;
}
