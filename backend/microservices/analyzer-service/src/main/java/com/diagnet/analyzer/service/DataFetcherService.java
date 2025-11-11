package com.diagnet.analyzer.service;

import com.diagnet.analyzer.model.MachineDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Fetcher Service
 * 
 * PURPOSE: Fetches machine data from collector-service REST API
 * WHY: Analyzer needs data to analyze - we call collector-service
 * 
 * USES: WebClient (reactive, non-blocking HTTP client)
 */
@Service
@Slf4j
public class DataFetcherService {

    private final WebClient webClient;
    
    public DataFetcherService(
            @Value("${services.collector.url}") String collectorUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(collectorUrl)
                .build();
        log.info("DataFetcherService initialized with collector URL: {}", collectorUrl);
    }
    
    /**
     * Fetch recent data for a specific machine
     * 
     * @param machineId Machine identifier (e.g., "M001")
     * @param hours How many hours of data to fetch
     * @return List of machine readings
     */
    public List<MachineDataDTO> fetchRecentData(String machineId, int hours) {
        log.debug("Fetching last {} hours of data for machine {}", hours, machineId);
        
        try {
            List<MachineDataDTO> data = webClient.get()
                    .uri("/api/data/machine/{machineId}/recent?hours={hours}", 
                         machineId, hours)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<MachineDataDTO>>() {})
                    .block(); // Block to make it synchronous (simpler for now)
            
            log.info("Fetched {} data points for machine {}", 
                    data != null ? data.size() : 0, machineId);
            return data != null ? data : List.of();
            
        } catch (Exception e) {
            log.error("Error fetching data for machine {}: {}", machineId, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Fetch all data in a time range for a machine
     * 
     * @param machineId Machine identifier
     * @param startTime Start of range
     * @param endTime End of range
     * @return List of readings
     */
    public List<MachineDataDTO> fetchDataInRange(
            String machineId, LocalDateTime startTime, LocalDateTime endTime) {
        
        log.debug("Fetching data for {} between {} and {}", 
                machineId, startTime, endTime);
        
        try {
            // First get recent data, then filter by time
            // (In production, you'd add this endpoint to collector-service)
            List<MachineDataDTO> data = fetchRecentData(machineId, 24);
            
            return data.stream()
                    .filter(d -> d.getTimestamp().isAfter(startTime) 
                              && d.getTimestamp().isBefore(endTime))
                    .toList();
                    
        } catch (Exception e) {
            log.error("Error fetching data range for machine {}: {}", 
                    machineId, e.getMessage());
            return List.of();
        }
    }
}
