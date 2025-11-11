package com.diagnet.collector.config;

import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT Configuration
 * 
 * WHY THIS CLASS EXISTS:
 * - Centralizes all MQTT settings
 * - Reads from application.yml (mqtt.* properties)
 * - Creates MqttConnectOptions bean for connection
 * 
 * @ConfigurationProperties("mqtt"):
 * - Binds to mqtt.* properties in application.yml
 * - Spring automatically fills fields from config file
 * 
 * EXAMPLE in application.yml:
 * mqtt:
 *   broker-url: tcp://localhost:1883
 *   client-id: collector-service
 *   username: admin
 *   password: admin
 */
@Configuration
@ConfigurationProperties(prefix = "mqtt")
@Data
public class MqttConfig {

    /**
     * MQTT broker URL
     * Format: tcp://host:port
     * Example: tcp://localhost:1883
     */
    private String brokerUrl;

    /**
     * Unique client ID for this service
     * Important: Each MQTT client needs unique ID
     * Using ${random.uuid} in application.yml ensures uniqueness
     */
    private String clientId;

    /**
     * MQTT broker username (optional)
     * Leave empty if broker doesn't require authentication
     */
    private String username;

    /**
     * MQTT broker password (optional)
     */
    private String password;

    /**
     * Topics to subscribe to
     * Example: ["machine/+/data", "machine/+/status"]
     * + is wildcard for single level
     */
    private String[] topics;

    /**
     * Connection timeout in seconds
     */
    private int connectionTimeout = 30;

    /**
     * Keep-alive interval in seconds
     * Broker will disconnect if no message for this duration
     */
    private int keepAliveInterval = 60;

    /**
     * Clean session flag
     * true: Start fresh (don't receive missed messages)
     * false: Receive messages sent while disconnected
     */
    private boolean cleanSession = true;

    /**
     * Automatic reconnect
     * true: Automatically reconnect if connection lost
     */
    private boolean automaticReconnect = true;

    /**
     * Create MQTT connection options bean
     * 
     * WHY @Bean:
     * - Spring manages this object
     * - Can be injected into other classes
     * - Configured once, used everywhere
     * 
     * @return Configured MQTT connection options
     */
    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        
        // Set broker URL
        options.setServerURIs(new String[]{brokerUrl});
        
        // Set credentials if provided
        if (username != null && !username.isEmpty()) {
            options.setUserName(username);
        }
        if (password != null && !password.isEmpty()) {
            options.setPassword(password.toCharArray());
        }
        
        // Connection settings
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setCleanSession(cleanSession);
        options.setAutomaticReconnect(automaticReconnect);
        
        return options;
    }
}
