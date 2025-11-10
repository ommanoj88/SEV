package com.evfleet.maintenance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * MaintenanceServiceConfig
 * Configuration for the Maintenance Service
 * 
 * @since 2.0.0 (PR 12: Maintenance Cost Tracking)
 */
@Configuration
public class MaintenanceServiceConfig {
    
    /**
     * RestTemplate bean for making HTTP requests to other services
     * Used by MaintenanceCostAnalyticsService to fetch vehicle information
     * 
     * @return RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
