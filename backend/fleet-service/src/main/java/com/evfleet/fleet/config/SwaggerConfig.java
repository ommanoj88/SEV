package com.evfleet.fleet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Configuration
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI fleetServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fleet Management Service API")
                        .description("REST API for Multi-Fuel Fleet Management - Vehicle Tracking, Telemetry, and Trip Management. " +
                                "Supports EV, ICE, and Hybrid vehicles with fuel-type-specific features and validations.")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("EV Fleet Management Team")
                                .email("support@evfleet.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
