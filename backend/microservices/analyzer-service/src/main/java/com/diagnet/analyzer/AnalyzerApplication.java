package com.diagnet.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Analyzer Service - Main Application
 * 
 * PURPOSE:
 * Analyzes machine data from collector-service to detect:
 * - Temperature anomalies
 * - Vibration anomalies
 * - Pattern deviations using statistical methods (Z-score)
 * 
 * RUNS ON: Port 8082
 */
@SpringBootApplication
public class AnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyzerApplication.class, args);
    }
}
