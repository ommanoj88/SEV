package com.evfleet.fleet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Fleet Service Application
 * Main microservice for managing EV fleet operations including:
 * - Vehicle management and tracking
 * - Real-time telemetry data processing
 * - Trip management and analytics
 * - Geofencing capabilities
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class FleetServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleetServiceApplication.class, args);
    }
}
