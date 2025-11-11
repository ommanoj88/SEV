package com.evfleet.analytics.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * AnalyticsServiceConfig
 * Configuration for the Analytics Service
 */
@Configuration
public class AnalyticsServiceConfig {
    
    /**
     * RestTemplate bean for making HTTP requests to other services
     * Configured with @LoadBalanced to use service discovery via Eureka
     * 
     * @return LoadBalanced RestTemplate instance
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
