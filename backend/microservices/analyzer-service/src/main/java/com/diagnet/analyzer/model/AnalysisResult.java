package com.diagnet.analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Analysis Result - Response from anomaly detection
 * 
 * WHY: Structured format for returning analysis results to clients
 * Contains overall health score and specific anomalies found
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult {
    
    private String machineId;
    private LocalDateTime analyzedAt;
    
    // Overall health score (0-100)
    // 100 = perfect, 0 = critical
    private Double healthScore;
    
    // Status: HEALTHY, WARNING, CRITICAL
    private String status;
    
    // List of detected anomalies
    private List<Anomaly> anomalies;
    
    // Statistics
    private Statistics statistics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Anomaly {
        private String type;           // TEMPERATURE, VIBRATION
        private String severity;       // WARNING, CRITICAL
        private Double value;
        private Double threshold;
        private String message;
        private LocalDateTime detectedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Double avgTemperature;
        private Double maxTemperature;
        private Double avgVibration;
        private Double maxVibration;
        private Integer dataPointsAnalyzed;
    }
}
