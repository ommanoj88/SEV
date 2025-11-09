package com.evfleet.gateway.filter;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import reactor.core.publisher.Mono;

/**
 * Global Authentication Filter
 * Validates Firebase tokens for all incoming requests
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh",
        "/api/auth/sync",
        "/api/auth/health",
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/refresh",
        "/api/v1/auth/sync",
        "/api/v1/auth/health",
        "/actuator",
        "/swagger-ui",
        "/api-docs"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Check if Firebase is initialized. If not, skip authentication (development mode)
        try {
            if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
                // Firebase not initialized - allow requests in dev mode but log a warning
                System.out.println("[AuthFilter] Firebase is not initialized - skipping token verification (dev mode)");
                return chain.filter(exchange);
            }
        } catch (Throwable t) {
            // Defensive: if any unexpected error occurs while checking Firebase, skip auth to avoid 500s
            System.out.println("[AuthFilter] Error checking Firebase initialization, skipping auth: " + t.getMessage());
            return chain.filter(exchange);
        }

        // Extract Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            // Verify Firebase token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            // Add user info to request headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", uid)
                .header("X-User-Email", email)
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (FirebaseAuthException e) {
            return onError(exchange, "Invalid or expired token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // High priority - execute before other filters
    }
}
