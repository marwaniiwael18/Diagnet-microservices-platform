package com.diagnet.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Collector Service
 * 
 * WHY THIS CLASS EXISTS:
 * - Every Spring Boot application needs a @SpringBootApplication annotated class
 * - This is where the application starts (main method)
 * - The @SpringBootApplication annotation does 3 things:
 *   1. @Configuration: Allows defining beans
 *   2. @EnableAutoConfiguration: Spring Boot auto-configures based on dependencies
 *   3. @ComponentScan: Scans for @Component, @Service, @Repository, @Controller
 * 
 * WHAT HAPPENS WHEN YOU RUN THIS:
 * 1. Spring Boot starts embedded Tomcat server on port 8081
 * 2. Scans all classes in com.diagnet.collector package
 * 3. Creates beans for @Service, @Repository, @Controller classes
 * 4. Connects to database using application.yml settings
 * 5. Starts MQTT listener
 * 6. Application is ready to receive requests!
 */
@SpringBootApplication
public class CollectorApplication {

    /**
     * Main method - application entry point
     * 
     * @param args Command line arguments (can be used for profiles, ports, etc.)
     */
    public static void main(String[] args) {
        SpringApplication.run(CollectorApplication.class, args);
    }
}
