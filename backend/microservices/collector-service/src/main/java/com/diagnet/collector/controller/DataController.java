package com.diagnet.collector.controller;

import com.diagnet.collector.model.MachineData;
import com.diagnet.collector.model.MachineDataDTO;
import com.diagnet.collector.service.DataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Machine Data API
 * 
 * WHY REST CONTROLLER:
 * - Exposes HTTP endpoints for external access
 * - Frontend, other services, or manual testing can send requests
 * - Alternative to MQTT (synchronous request/response)
 * 
 * ANNOTATIONS:
 * - @RestController: Combines @Controller + @ResponseBody (returns JSON)
 * - @RequestMapping: Base path for all endpoints (/api/data)
 * - @CrossOrigin: Allows frontend from different domain (CORS)
 * - @RequiredArgsConstructor: Injects dependencies
 * 
 * ENDPOINT PATTERN:
 * - POST: Create new data
 * - GET: Retrieve data
 * - No PUT/DELETE: We don't modify sensor data once stored
 * 
 * BASE URL: http://localhost:8081/api/data
 */
@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*")  // Allow all origins (restrict in production!)
@RequiredArgsConstructor
@Slf4j
public class DataController {

    private final DataService dataService;

    /**
     * POST /api/data
     * Submit new machine data
     * 
     * EXAMPLE REQUEST:
     * curl -X POST http://localhost:8081/api/data \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "machineId": "M001",
     *     "timestamp": "2025-11-11T10:30:00",
     *     "temperature": 75.5,
     *     "vibration": 0.3,
     *     "status": "running"
     *   }'
     * 
     * VALIDATION:
     * - @Valid triggers validation rules in MachineDataDTO
     * - If validation fails, returns 400 Bad Request automatically
     * 
     * @param dto Machine data (validated)
     * @return 201 Created with saved data
     */
    @PostMapping
    public ResponseEntity<MachineData> submitData(@Valid @RequestBody MachineDataDTO dto) {
        log.info("Received data submission for machine: {}", dto.getMachineId());
        
        try {
            // Additional business validation
            if (!dataService.validateDataQuality(dto)) {
                return ResponseEntity.badRequest().build();
            }
            
            // Save to database
            MachineData saved = dataService.saveData(dto);
            
            // Return 201 Created with the saved entity
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
            
        } catch (Exception e) {
            log.error("Error saving data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/data/machine/{machineId}
     * Get all data for a specific machine
     * 
     * EXAMPLE: GET http://localhost:8081/api/data/machine/M001
     * 
     * @param machineId Machine identifier from URL path
     * @return List of all readings for that machine
     */
    @GetMapping("/machine/{machineId}")
    public ResponseEntity<List<MachineData>> getDataByMachine(
            @PathVariable String machineId) {
        
        log.info("Fetching data for machine: {}", machineId);
        List<MachineData> data = dataService.getDataByMachine(machineId);
        return ResponseEntity.ok(data);
    }

    /**
     * GET /api/data/machine/{machineId}/recent?hours=24
     * Get recent data for a machine (last N hours)
     * 
     * EXAMPLE: GET http://localhost:8081/api/data/machine/M001/recent?hours=24
     * 
     * @param machineId Machine identifier
     * @param hours Number of hours to look back (default: 24)
     * @return Recent readings
     */
    @GetMapping("/machine/{machineId}/recent")
    public ResponseEntity<List<MachineData>> getRecentDataByMachine(
            @PathVariable String machineId,
            @RequestParam(defaultValue = "24") int hours) {
        
        log.info("Fetching last {} hours of data for machine: {}", hours, machineId);
        List<MachineData> data = dataService.getRecentDataByMachine(machineId, hours);
        return ResponseEntity.ok(data);
    }

    /**
     * GET /api/data/recent?limit=100
     * Get most recent N readings across all machines
     * 
     * EXAMPLE: GET http://localhost:8081/api/data/recent?limit=50
     * 
     * @param limit Number of records (default: 100)
     * @return Most recent readings
     */
    @GetMapping("/recent")
    public ResponseEntity<List<MachineData>> getRecentData(
            @RequestParam(defaultValue = "100") int limit) {
        
        log.info("Fetching {} most recent readings", limit);
        List<MachineData> data = dataService.getRecentData(limit);
        return ResponseEntity.ok(data);
    }

    /**
     * GET /api/data/range?start=2025-11-10T00:00:00&end=2025-11-11T23:59:59
     * Get data in a specific time range
     * 
     * EXAMPLE: 
     * GET http://localhost:8081/api/data/range?start=2025-11-10T00:00:00&end=2025-11-11T23:59:59
     * 
     * @param start Start of time range (ISO format)
     * @param end End of time range
     * @return All readings in that range
     */
    @GetMapping("/range")
    public ResponseEntity<List<MachineData>> getDataInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        log.info("Fetching data between {} and {}", start, end);
        List<MachineData> data = dataService.getDataInTimeRange(start, end);
        return ResponseEntity.ok(data);
    }

    /**
     * GET /api/data/status/{status}
     * Get data by status (e.g., all errored machines)
     * 
     * EXAMPLE: GET http://localhost:8081/api/data/status/error
     * 
     * @param status Status to filter by (running, idle, error, maintenance)
     * @return Readings with that status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MachineData>> getDataByStatus(
            @PathVariable String status) {
        
        log.info("Fetching data with status: {}", status);
        List<MachineData> data = dataService.getDataByStatus(status);
        return ResponseEntity.ok(data);
    }

    /**
     * GET /api/data/alerts/temperature?threshold=100&hours=24
     * Get machines with high temperature
     * 
     * EXAMPLE: GET http://localhost:8081/api/data/alerts/temperature?threshold=100&hours=1
     * 
     * @param threshold Minimum temperature (default: 100°C)
     * @param hours Look back N hours (default: 24)
     * @return Readings exceeding threshold
     */
    @GetMapping("/alerts/temperature")
    public ResponseEntity<List<MachineData>> getTemperatureAlerts(
            @RequestParam(defaultValue = "100.0") Double threshold,
            @RequestParam(defaultValue = "24") int hours) {
        
        log.info("Checking for temperature alerts above {}°C in last {} hours", threshold, hours);
        List<MachineData> alerts = dataService.getHighTemperatureAlerts(threshold, hours);
        return ResponseEntity.ok(alerts);
    }

    /**
     * GET /api/data/alerts/vibration?threshold=0.8&hours=24
     * Get machines with high vibration
     * 
     * EXAMPLE: GET http://localhost:8081/api/data/alerts/vibration?threshold=0.8&hours=1
     * 
     * @param threshold Minimum vibration (default: 0.8)
     * @param hours Look back N hours (default: 24)
     * @return Readings exceeding threshold
     */
    @GetMapping("/alerts/vibration")
    public ResponseEntity<List<MachineData>> getVibrationAlerts(
            @RequestParam(defaultValue = "0.8") Double threshold,
            @RequestParam(defaultValue = "24") int hours) {
        
        log.info("Checking for vibration alerts above {} in last {} hours", threshold, hours);
        List<MachineData> alerts = dataService.getHighVibrationAlerts(threshold, hours);
        return ResponseEntity.ok(alerts);
    }

    /**
     * GET /api/data/machine/{machineId}/stats?start=...&end=...
     * Get statistics for a machine in a time period
     * 
     * EXAMPLE: 
     * GET http://localhost:8081/api/data/machine/M001/stats?start=2025-11-10T00:00:00&end=2025-11-11T23:59:59
     * 
     * @param machineId Machine to analyze
     * @param start Start of period
     * @param end End of period
     * @return Statistics (average temperature, count, etc.)
     */
    @GetMapping("/machine/{machineId}/stats")
    public ResponseEntity<Map<String, Object>> getMachineStats(
            @PathVariable String machineId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        log.info("Calculating stats for machine {} between {} and {}", machineId, start, end);
        
        Double avgTemp = dataService.getAverageTemperature(machineId, start, end);
        long count = dataService.getCountByMachine(machineId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("machineId", machineId);
        stats.put("averageTemperature", avgTemp);
        stats.put("totalReadings", count);
        stats.put("startTime", start);
        stats.put("endTime", end);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/data/health
     * Health check endpoint
     * 
     * EXAMPLE: GET http://localhost:8081/api/data/health
     * 
     * @return Simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "collector-service");
        health.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
}
