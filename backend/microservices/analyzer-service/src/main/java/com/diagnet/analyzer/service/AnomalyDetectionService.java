package com.diagnet.analyzer.service;

import com.diagnet.analyzer.model.AnalysisResult;
import com.diagnet.analyzer.model.MachineDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Anomaly Detection Service
 * 
 * PURPOSE: Analyzes machine data to detect anomalies using statistical methods
 * 
 * METHODS USED:
 * 1. Z-Score Analysis - detects values far from the mean
 * 2. Threshold Checking - compares against fixed limits
 * 
 * WHY Z-SCORE:
 * Z-score = (value - mean) / standard_deviation
 * If |Z-score| > 3, the value is an outlier (3 standard deviations away)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionService {

    private final DataFetcherService dataFetcher;
    
    @Value("${analysis.z-score-threshold}")
    private double zScoreThreshold;
    
    @Value("${analysis.min-data-points}")
    private int minDataPoints;
    
    @Value("${analysis.temperature.warning}")
    private double tempWarning;
    
    @Value("${analysis.temperature.critical}")
    private double tempCritical;
    
    @Value("${analysis.vibration.warning}")
    private double vibWarning;
    
    @Value("${analysis.vibration.critical}")
    private double vibCritical;
    
    /**
     * Analyze machine for anomalies
     * 
     * FLOW:
     * 1. Fetch recent data
     * 2. Check if enough data points
     * 3. Calculate statistics
     * 4. Detect threshold violations
     * 5. Detect statistical anomalies (Z-score)
     * 6. Calculate health score
     * 7. Return results
     */
    public AnalysisResult analyzeMachine(String machineId, int hours) {
        log.info("Starting analysis for machine: {}", machineId);
        
        // 1. Fetch data
        List<MachineDataDTO> data = dataFetcher.fetchRecentData(machineId, hours);
        
        // 2. Check minimum data points
        if (data.size() < minDataPoints) {
            log.warn("Not enough data for machine {}. Need {}, got {}", 
                    machineId, minDataPoints, data.size());
            return buildInsufficientDataResult(machineId, data.size());
        }
        
        // 3. Calculate statistics
        DescriptiveStatistics tempStats = new DescriptiveStatistics();
        DescriptiveStatistics vibStats = new DescriptiveStatistics();
        
        for (MachineDataDTO reading : data) {
            tempStats.addValue(reading.getTemperature());
            vibStats.addValue(reading.getVibration());
        }
        
        // 4. Detect anomalies
        List<AnalysisResult.Anomaly> anomalies = new ArrayList<>();
        
        // Check threshold violations
        anomalies.addAll(detectThresholdAnomalies(data));
        
        // Check statistical anomalies
        anomalies.addAll(detectStatisticalAnomalies(data, tempStats, vibStats));
        
        // 5. Calculate health score
        double healthScore = calculateHealthScore(anomalies, data.size());
        String status = determineStatus(healthScore);
        
        // 6. Build statistics
        AnalysisResult.Statistics statistics = AnalysisResult.Statistics.builder()
                .avgTemperature(tempStats.getMean())
                .maxTemperature(tempStats.getMax())
                .avgVibration(vibStats.getMean())
                .maxVibration(vibStats.getMax())
                .dataPointsAnalyzed(data.size())
                .build();
        
        log.info("Analysis complete for {}. Health score: {}, Anomalies: {}", 
                machineId, healthScore, anomalies.size());
        
        return AnalysisResult.builder()
                .machineId(machineId)
                .analyzedAt(LocalDateTime.now())
                .healthScore(healthScore)
                .status(status)
                .anomalies(anomalies)
                .statistics(statistics)
                .build();
    }
    
    /**
     * Detect threshold violations (fixed limits)
     */
    private List<AnalysisResult.Anomaly> detectThresholdAnomalies(List<MachineDataDTO> data) {
        List<AnalysisResult.Anomaly> anomalies = new ArrayList<>();
        
        for (MachineDataDTO reading : data) {
            // Temperature checks
            if (reading.getTemperature() >= tempCritical) {
                anomalies.add(AnalysisResult.Anomaly.builder()
                        .type("TEMPERATURE")
                        .severity("CRITICAL")
                        .value(reading.getTemperature())
                        .threshold(tempCritical)
                        .message("Temperature critically high: " + reading.getTemperature() + "°C")
                        .detectedAt(reading.getTimestamp())
                        .build());
            } else if (reading.getTemperature() >= tempWarning) {
                anomalies.add(AnalysisResult.Anomaly.builder()
                        .type("TEMPERATURE")
                        .severity("WARNING")
                        .value(reading.getTemperature())
                        .threshold(tempWarning)
                        .message("Temperature warning: " + reading.getTemperature() + "°C")
                        .detectedAt(reading.getTimestamp())
                        .build());
            }
            
            // Vibration checks
            if (reading.getVibration() >= vibCritical) {
                anomalies.add(AnalysisResult.Anomaly.builder()
                        .type("VIBRATION")
                        .severity("CRITICAL")
                        .value(reading.getVibration())
                        .threshold(vibCritical)
                        .message("Vibration critically high: " + reading.getVibration())
                        .detectedAt(reading.getTimestamp())
                        .build());
            } else if (reading.getVibration() >= vibWarning) {
                anomalies.add(AnalysisResult.Anomaly.builder()
                        .type("VIBRATION")
                        .severity("WARNING")
                        .value(reading.getVibration())
                        .threshold(vibWarning)
                        .message("Vibration warning: " + reading.getVibration())
                        .detectedAt(reading.getTimestamp())
                        .build());
            }
        }
        
        return anomalies;
    }
    
    /**
     * Detect statistical anomalies using Z-score
     * 
     * Z-score tells us how many standard deviations a value is from the mean
     * |Z| > 3 means the value is very unusual (99.7% of data is within 3σ)
     */
    private List<AnalysisResult.Anomaly> detectStatisticalAnomalies(
            List<MachineDataDTO> data,
            DescriptiveStatistics tempStats,
            DescriptiveStatistics vibStats) {
        
        List<AnalysisResult.Anomaly> anomalies = new ArrayList<>();
        
        double tempMean = tempStats.getMean();
        double tempStdDev = tempStats.getStandardDeviation();
        double vibMean = vibStats.getMean();
        double vibStdDev = vibStats.getStandardDeviation();
        
        for (MachineDataDTO reading : data) {
            // Temperature Z-score
            if (tempStdDev > 0) {
                double tempZScore = Math.abs((reading.getTemperature() - tempMean) / tempStdDev);
                if (tempZScore > zScoreThreshold) {
                    anomalies.add(AnalysisResult.Anomaly.builder()
                            .type("TEMPERATURE")
                            .severity("WARNING")
                            .value(reading.getTemperature())
                            .threshold(tempMean + (zScoreThreshold * tempStdDev))
                            .message("Unusual temperature pattern detected (Z-score: " 
                                    + String.format("%.2f", tempZScore) + ")")
                            .detectedAt(reading.getTimestamp())
                            .build());
                }
            }
            
            // Vibration Z-score
            if (vibStdDev > 0) {
                double vibZScore = Math.abs((reading.getVibration() - vibMean) / vibStdDev);
                if (vibZScore > zScoreThreshold) {
                    anomalies.add(AnalysisResult.Anomaly.builder()
                            .type("VIBRATION")
                            .severity("WARNING")
                            .value(reading.getVibration())
                            .threshold(vibMean + (zScoreThreshold * vibStdDev))
                            .message("Unusual vibration pattern detected (Z-score: " 
                                    + String.format("%.2f", vibZScore) + ")")
                            .detectedAt(reading.getTimestamp())
                            .build());
                }
            }
        }
        
        return anomalies;
    }
    
    /**
     * Calculate overall health score (0-100)
     * 
     * Formula:
     * - Start with 100
     * - Subtract 20 for each CRITICAL anomaly
     * - Subtract 5 for each WARNING anomaly
     * - Minimum score is 0
     */
    private double calculateHealthScore(List<AnalysisResult.Anomaly> anomalies, int dataPoints) {
        double score = 100.0;
        
        long criticalCount = anomalies.stream()
                .filter(a -> "CRITICAL".equals(a.getSeverity()))
                .count();
        
        long warningCount = anomalies.stream()
                .filter(a -> "WARNING".equals(a.getSeverity()))
                .count();
        
        score -= (criticalCount * 20);
        score -= (warningCount * 5);
        
        return Math.max(0, score);
    }
    
    /**
     * Determine status based on health score
     */
    private String determineStatus(double healthScore) {
        if (healthScore >= 80) return "HEALTHY";
        if (healthScore >= 50) return "WARNING";
        return "CRITICAL";
    }
    
    /**
     * Build result for insufficient data
     */
    private AnalysisResult buildInsufficientDataResult(String machineId, int dataPoints) {
        return AnalysisResult.builder()
                .machineId(machineId)
                .analyzedAt(LocalDateTime.now())
                .healthScore(null)
                .status("INSUFFICIENT_DATA")
                .anomalies(List.of())
                .statistics(AnalysisResult.Statistics.builder()
                        .dataPointsAnalyzed(dataPoints)
                        .build())
                .build();
    }
}
