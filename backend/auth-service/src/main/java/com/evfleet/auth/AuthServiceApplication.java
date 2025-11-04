package com.evfleet.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Authentication Service Application
 * Handles user authentication, authorization, and user management
 * for the EV Fleet Management Platform
 *
 * @author EV Fleet Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        System.out.println("=========================================");
        System.out.println("Auth Service Started Successfully!");
        System.out.println("Service URL: http://localhost:8081");
        System.out.println("Swagger UI: http://localhost:8081/swagger-ui.html");
        System.out.println("=========================================");
    }
}
