package com.evfleet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * EV Fleet Management Platform - Modular Monolith
 *
 * Main application entry point for the consolidated monolithic architecture.
 * This application consolidates all microservices into a single deployable unit
 * while maintaining modular boundaries using Spring Modulith.
 *
 * Modules:
 * - auth: Authentication and authorization
 * - fleet: Vehicle and fleet management
 * - charging: Charging infrastructure and sessions
 * - maintenance: Vehicle maintenance and battery health
 * - driver: Driver management and performance
 * - analytics: Cost analytics and reporting
 * - notification: Alert and notification management
 * - billing: Subscription and invoice management
 *
 * @author SEV Platform Team
 * @version 1.0.0
 * @since 2025-11-12
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
public class EvFleetApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvFleetApplication.class, args);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("EVFleet Modular Monolith Started Successfully!");
        System.out.println("=".repeat(60));
        System.out.println("Service URL: http://localhost:8080");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("Actuator: http://localhost:8080/actuator/health");
        System.out.println("=".repeat(60) + "\n");
    }
}
