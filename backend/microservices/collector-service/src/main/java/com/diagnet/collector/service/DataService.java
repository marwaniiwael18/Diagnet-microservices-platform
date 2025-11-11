package com.diagnet.collector.service;

import com.diagnet.collector.model.MachineData;
import com.diagnet.collector.model.MachineDataDTO;
import com.diagnet.collector.repository.MachineDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for MachineData business logic
 * 
 * WHY SERVICE LAYER:
 * - Separates business logic from controllers (clean code)
 * - Can be reused by multiple controllers or MQTT listener
 * - Handles transactions, validation, logging
 * - Makes testing easier (mock the service)
 * 
 * ANNOTATIONS:
 * - @Service: Spring creates a bean of this class
 * - @RequiredArgsConstructor (Lombok): Auto-generates constructor for final fields
 * - @Slf4j (Lombok): Auto-creates logger (log.info(), log.error(), etc.)
 * - @Transactional: Database operations in one transaction (all-or-nothing)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataService {

    // Automatically injected by Spring (via @RequiredArgsConstructor)
    private final MachineDataRepository repository;

    /**
     * Save machine data to database
     * 
     * FLOW:
     * 1. Receive DTO from controller or MQTT listener
     * 2. Convert DTO to Entity
     * 3. Save to database
     * 4. Log success
     * 5. Return saved entity (with generated ID)
     * 
     * @param dto Data transfer object with validated data
     * @return Saved entity with generated ID
     * @throws Exception if database save fails
     */
    @Transactional
    public MachineData saveData(MachineDataDTO dto) {
        log.debug("Saving data for machine: {}", dto.getMachineId());
        
        try {
            // Convert DTO to Entity
            MachineData entity = dto.toEntity();
            
            // Save to database
            MachineData saved = repository.save(entity);
            
            log.info("Successfully saved data for machine {} at {}", 
                    saved.getMachineId(), saved.getTimestamp());
            
            return saved;
            
        } catch (Exception e) {
            log.error("Failed to save data for machine {}: {}", 
                    dto.getMachineId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Get all data for a specific machine
     * 
     * @param machineId Machine identifier
     * @return List of all readings for that machine
     */
    public List<MachineData> getDataByMachine(String machineId) {
        log.debug("Fetching all data for machine: {}", machineId);
        return repository.findByMachineIdOrderByTimestampDesc(machineId);
    }

    /**
     * Get recent data for a machine (last N hours)
     * 
     * @param machineId Machine identifier
     * @param hours Number of hours to look back
     * @return Recent readings for that machine
     */
    public List<MachineData> getRecentDataByMachine(String machineId, int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        log.debug("Fetching data for machine {} from last {} hours", machineId, hours);
        return repository.findByMachineIdAndTimestampAfterOrderByTimestampDesc(
                machineId, startTime);
    }

    /**
     * Get all data in a time range
     * 
     * @param startTime Start of range
     * @param endTime End of range
     * @return All readings in that range
     */
    public List<MachineData> getDataInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Fetching data between {} and {}", startTime, endTime);
        return repository.findByTimestampBetweenOrderByTimestampDesc(startTime, endTime);
    }

    /**
     * Get most recent N readings (across all machines)
     * 
     * @param limit Number of recent records to return
     * @return Most recent readings
     */
    public List<MachineData> getRecentData(int limit) {
        log.debug("Fetching {} most recent readings", limit);
        return repository.findRecentData(limit);
    }

    /**
     * Get data by status (e.g., all errored machines)
     * 
     * @param status Status to filter by
     * @return Readings with that status
     */
    public List<MachineData> getDataByStatus(String status) {
        log.debug("Fetching data with status: {}", status);
        return repository.findByStatusOrderByTimestampDesc(status);
    }

    /**
     * Get count of readings for a machine
     * 
     * @param machineId Machine identifier
     * @return Total number of readings
     */
    public long getCountByMachine(String machineId) {
        return repository.countByMachineId(machineId);
    }

    /**
     * Find machines with high temperature (potential issues)
     * 
     * @param minTemperature Temperature threshold (e.g., 100°C)
     * @param hours Look back N hours
     * @return Readings exceeding threshold
     */
    public List<MachineData> getHighTemperatureAlerts(Double minTemperature, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        log.debug("Checking for temperatures above {}°C in last {} hours", minTemperature, hours);
        return repository.findHighTemperatureReadings(minTemperature, since);
    }

    /**
     * Find machines with high vibration (potential mechanical issues)
     * 
     * @param minVibration Vibration threshold (e.g., 0.8)
     * @param hours Look back N hours
     * @return Readings exceeding threshold
     */
    public List<MachineData> getHighVibrationAlerts(Double minVibration, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        log.debug("Checking for vibration above {} in last {} hours", minVibration, hours);
        return repository.findHighVibrationReadings(minVibration, since);
    }

    /**
     * Calculate average temperature for a machine in a time period
     * 
     * @param machineId Machine to analyze
     * @param startTime Start of period
     * @param endTime End of period
     * @return Average temperature, or null if no data
     */
    public Double getAverageTemperature(String machineId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Calculating average temperature for {} between {} and {}", 
                machineId, startTime, endTime);
        return repository.getAverageTemperature(machineId, startTime, endTime);
    }

    /**
     * Delete old data (data retention)
     * 
     * WARNING: This permanently deletes data!
     * 
     * @param days Delete data older than N days
     * @return Number of records deleted
     */
    @Transactional
    public long deleteOldData(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        log.warn("Deleting data older than {} days (before {})", days, cutoffDate);
        
        long deletedCount = repository.deleteByTimestampBefore(cutoffDate);
        log.info("Deleted {} old records", deletedCount);
        
        return deletedCount;
    }

    /**
     * Validate data quality (business rules)
     * 
     * This is called AFTER basic validation (@Valid in controller)
     * Add custom business logic here
     * 
     * @param dto Data to validate
     * @return true if data passes all checks
     */
    public boolean validateDataQuality(MachineDataDTO dto) {
        // Example business rules:
        
        // 1. If status is "error", temperature should be high or vibration should be high
        if ("error".equals(dto.getStatus())) {
            if (dto.getTemperature() < 50 && dto.getVibration() < 0.5) {
                log.warn("Machine {} has status 'error' but normal readings", dto.getMachineId());
                return false;
            }
        }
        
        // 2. If status is "idle", temperature should be relatively low
        if ("idle".equals(dto.getStatus())) {
            if (dto.getTemperature() > 80) {
                log.warn("Machine {} is 'idle' but temperature is {}°C", 
                        dto.getMachineId(), dto.getTemperature());
                return false;
            }
        }
        
        // 3. Vibration and temperature often correlate
        if (dto.getVibration() > 0.8 && dto.getTemperature() < 40) {
            log.warn("Unusual: High vibration ({}) but low temperature ({}°C) for machine {}", 
                    dto.getVibration(), dto.getTemperature(), dto.getMachineId());
            // Don't reject, but log for investigation
        }
        
        return true;
    }
}
