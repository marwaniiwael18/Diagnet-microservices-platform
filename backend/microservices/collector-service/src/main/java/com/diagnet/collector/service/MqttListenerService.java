package com.diagnet.collector.service;

import com.diagnet.collector.config.MqttConfig;
import com.diagnet.collector.model.MachineDataDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

/**
 * MQTT Listener Service
 * 
 * WHY THIS CLASS EXISTS:
 * - Connects to MQTT broker
 * - Subscribes to machine data topics
 * - Listens for incoming messages
 * - Converts JSON messages to DTOs
 * - Saves data to database
 * 
 * LIFECYCLE:
 * - @PostConstruct: Called after Spring creates this bean → connects to broker
 * - @PreDestroy: Called before Spring shuts down → disconnects cleanly
 * 
 * MQTT PUB/SUB PATTERN:
 * - Machines (publishers) send data to topics like "machine/1/data"
 * - This service (subscriber) listens to all "machine/+/data" topics
 * - Broker (Mosquitto) manages the message routing
 * 
 * EXAMPLE MESSAGE:
 * Topic: machine/1/data
 * Payload: {"machineId":"M001","timestamp":"2025-11-11T10:30:00","temperature":75.5,"vibration":0.3,"status":"running"}
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MqttListenerService implements MqttCallback {

    private final MqttConfig mqttConfig;
    private final DataService dataService;
    private final MqttConnectOptions connectOptions;
    
    private MqttClient mqttClient;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // For LocalDateTime parsing

    /**
     * Initialize MQTT connection after Spring creates this bean
     * 
     * FLOW:
     * 1. Create MQTT client with unique ID
     * 2. Set this class as callback (for handling messages)
     * 3. Connect to broker with options
     * 4. Subscribe to configured topics
     * 5. Log success
     * 
     * @throws MqttException if connection fails
     */
    @PostConstruct
    public void initialize() {
        try {
            log.info("Initializing MQTT connection to {}", mqttConfig.getBrokerUrl());
            
            // Create MQTT client
            mqttClient = new MqttClient(
                    mqttConfig.getBrokerUrl(),
                    mqttConfig.getClientId()
            );
            
            // Set callback handler (this class implements MqttCallback)
            mqttClient.setCallback(this);
            
            // Connect to broker
            mqttClient.connect(connectOptions);
            log.info("Successfully connected to MQTT broker");
            
            // Subscribe to topics
            for (String topic : mqttConfig.getTopics()) {
                mqttClient.subscribe(topic);
                log.info("Subscribed to topic: {}", topic);
            }
            
        } catch (MqttException e) {
            log.error("Failed to initialize MQTT connection: {}", e.getMessage());
            log.error("Make sure MQTT broker is running at {}", mqttConfig.getBrokerUrl());
        }
    }

    /**
     * Disconnect from MQTT broker before shutdown
     * 
     * IMPORTANT: Always disconnect cleanly!
     * - Prevents "ghost" connections on broker
     * - Allows graceful shutdown
     */
    @PreDestroy
    public void destroy() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                log.info("Disconnecting from MQTT broker");
                mqttClient.disconnect();
                mqttClient.close();
                log.info("Successfully disconnected from MQTT broker");
            } catch (MqttException e) {
                log.error("Error disconnecting from MQTT broker: {}", e.getMessage());
            }
        }
    }

    /**
     * Handle connection lost (MqttCallback interface)
     * 
     * WHEN THIS IS CALLED:
     * - Broker goes down
     * - Network issues
     * - Keep-alive timeout
     * 
     * WHAT HAPPENS:
     * - Log the error
     * - If automaticReconnect=true, Eclipse Paho will reconnect automatically
     * 
     * @param cause Exception that caused disconnection
     */
    @Override
    public void connectionLost(Throwable cause) {
        log.error("MQTT connection lost: {}", cause.getMessage());
        log.info("Will attempt to reconnect automatically...");
    }

    /**
     * Handle incoming MQTT message (MqttCallback interface)
     * 
     * THIS IS THE MAIN METHOD - called whenever a message arrives!
     * 
     * FLOW:
     * 1. Message arrives on subscribed topic
     * 2. Extract topic and payload
     * 3. Parse JSON payload to DTO
     * 4. Validate DTO
     * 5. Save to database via DataService
     * 6. Log success
     * 
     * ERROR HANDLING:
     * - If parsing fails: Log error, don't crash
     * - If validation fails: Log error, don't save
     * - If database fails: DataService handles it
     * 
     * @param topic Topic the message arrived on (e.g., "machine/1/data")
     * @param message MQTT message containing payload
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            log.debug("Message arrived on topic: {}", topic);
            
            // Convert byte[] payload to String
            String payload = new String(message.getPayload());
            log.debug("Payload: {}", payload);
            
            // Parse JSON to DTO
            MachineDataDTO dto = objectMapper.readValue(payload, MachineDataDTO.class);
            log.info("Received data from machine {} via MQTT", dto.getMachineId());
            
            // Validate data quality
            if (!dataService.validateDataQuality(dto)) {
                log.warn("Data quality validation failed for machine {}", dto.getMachineId());
                return; // Don't save invalid data
            }
            
            // Save to database
            dataService.saveData(dto);
            log.info("Successfully processed MQTT message for machine {}", dto.getMachineId());
            
        } catch (Exception e) {
            log.error("Error processing MQTT message from topic {}: {}", topic, e.getMessage());
            log.debug("Payload that caused error: {}", new String(message.getPayload()));
        }
    }

    /**
     * Handle message delivery complete (MqttCallback interface)
     * 
     * WHEN THIS IS CALLED:
     * - When WE publish a message (we're only subscribing, so not used here)
     * 
     * @param token Delivery token for the published message
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not used in this service (we only subscribe, don't publish)
        log.debug("Message delivery complete (not used in subscriber)");
    }

    /**
     * Check if MQTT client is connected
     * 
     * USEFUL FOR:
     * - Health checks
     * - Monitoring
     * - Debugging
     * 
     * @return true if connected to broker
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    /**
     * Get connection status for monitoring
     * 
     * @return Connection details
     */
    public String getConnectionStatus() {
        if (mqttClient == null) {
            return "MQTT client not initialized";
        }
        if (mqttClient.isConnected()) {
            return "Connected to " + mqttConfig.getBrokerUrl();
        }
        return "Disconnected from " + mqttConfig.getBrokerUrl();
    }
}
