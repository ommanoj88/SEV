package com.evfleet.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server Application
 * Centralized Configuration Management for EV Fleet Management Platform
 *
 * This server provides externalized configuration for all microservices,
 * allowing dynamic configuration updates without restarting services.
 *
 * @author EV Fleet Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
        System.out.println("======================================");
        System.out.println("Config Server Started Successfully!");
        System.out.println("Configuration Endpoint: http://localhost:8888");
        System.out.println("======================================");
    }
}
