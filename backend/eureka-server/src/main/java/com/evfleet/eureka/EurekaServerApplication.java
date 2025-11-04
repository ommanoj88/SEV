package com.evfleet.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server Application
 * Service Discovery for EV Fleet Management Platform
 *
 * This server acts as a registry where all microservices register themselves
 * and discover other services for inter-service communication.
 *
 * @author EV Fleet Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
        System.out.println("===================================");
        System.out.println("Eureka Server Started Successfully!");
        System.out.println("Dashboard: http://localhost:8761");
        System.out.println("===================================");
    }
}
