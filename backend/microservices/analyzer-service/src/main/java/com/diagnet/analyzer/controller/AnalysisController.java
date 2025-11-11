package com.diagnet.analyzer.controller;

import com.diagnet.analyzer.model.AnalysisResult;
import com.diagnet.analyzer.service.AnomalyDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Analysis Controller - REST API for anomaly detection
 * 
 * ENDPOINTS:
 * - GET /api/analysis/machine/{machineId} - analyze specific machine
 * - GET /api/analysis/health - service health check
 * 
 * PORT: 8082
 */
@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AnomalyDetectionService anomalyDetectionService;
    
    /**
     * Analyze a specific machine for anomalies
     * 
     * EXAMPLE:
     * GET http://localhost:8082/api/analysis/machine/M001?hours=24
     * 
     * RESPONSE:
     * {
     *   "machineId": "M001",
     *   "healthScore": 85.0,
     *   "status": "HEALTHY",
     *   "anomalies": [...],
     *   "statistics": {...}
     * }
     */
    @GetMapping("/machine/{machineId}")
    public ResponseEntity<AnalysisResult> analyzeMachine(
            @PathVariable String machineId,
            @RequestParam(defaultValue = "24") int hours) {
        
        log.info("Received analysis request for machine: {}, hours: {}", machineId, hours);
        
        try {
            AnalysisResult result = anomalyDetectionService.analyzeMachine(machineId, hours);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error analyzing machine {}: {}", machineId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Health check endpoint
     * 
     * EXAMPLE: GET http://localhost:8082/api/analysis/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "analyzer-service",
                "port", "8082"
        ));
    }
}
