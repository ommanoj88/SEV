package com.evfleet.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application
 * Single Entry Point for EV Fleet Management Platform
 *
 * This gateway routes all client requests to appropriate microservices,
 * handles authentication, rate limiting, and load balancing.
 *
 * @author EV Fleet Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("======================================");
        System.out.println("API Gateway Started Successfully!");
        System.out.println("Gateway URL: http://localhost:8080");
        System.out.println("======================================");
    }
}
