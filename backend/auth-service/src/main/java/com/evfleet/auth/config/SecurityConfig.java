package com.evfleet.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration
 * Configures Spring Security for the Auth Service
 * Currently allows all endpoints for development/testing
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    /**
     * Configure HTTP Security
     * Currently permits all requests for ease of development
     * TODO: Implement proper authentication/authorization for production
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");

        http
                // Disable CSRF for REST API
                .csrf(AbstractHttpConfigurer::disable)

                // Disable CORS - handled by API Gateway
                .cors(AbstractHttpConfigurer::disable)

                // Configure session management (stateless for REST API)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure authorization
                .authorizeHttpRequests(auth -> auth
                        // Allow actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()

                        // Allow Swagger/OpenAPI endpoints
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Allow health check
                        .requestMatchers("/api/auth/health").permitAll()

                        // Allow all auth endpoints for now
                        // TODO: Implement proper authentication for sensitive endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Require authentication for all other requests
                        .anyRequest().authenticated()
                );

        log.info("Security filter chain configured successfully");
        return http.build();
    }

    /**
     * CORS Configuration
     * Allows cross-origin requests from frontend applications
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS");

        CorsConfiguration configuration = new CorsConfiguration();

        // Allow all origins for development
        // TODO: Restrict to specific origins in production
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Allow common headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Expose headers
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Authorization"
        ));

        // Allow credentials
        configuration.setAllowCredentials(true);

        // Max age for preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS configured successfully");
        return source;
    }
}
