package com.evfleet.routing.controller;

import com.evfleet.common.dto.ApiResponse;
import com.evfleet.routing.dto.RoutePlanRequest;
import com.evfleet.routing.dto.RoutePlanResponse;
import com.evfleet.routing.dto.WaypointRequest;
import com.evfleet.routing.dto.WaypointResponse;
import com.evfleet.routing.model.RoutePlan;
import com.evfleet.routing.model.RouteWaypoint;
import com.evfleet.routing.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Route Planning Controller
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Route Planning", description = "Route Planning & Optimization API")
public class RouteController {

    private final RouteService routeService;

    /**
     * Get all routes
     */
    @GetMapping
    @Operation(summary = "Get all routes for a company")
    public ResponseEntity<ApiResponse<List<RoutePlanResponse>>> getAllRoutes(
            @RequestParam(required = false) Long companyId) {
        log.info("GET /api/routes - companyId: {}", companyId);

        if (companyId == null) {
            return ResponseEntity.ok(ApiResponse.success(
                "Company ID is required", List.of()));
        }

        List<RoutePlanResponse> routes = routeService.getRoutesByCompany(companyId)
            .stream()
            .map(RoutePlanResponse::fromWithoutWaypoints)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
            "Routes retrieved successfully", routes));
    }

    /**
     * Get route by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get route by ID")
    public ResponseEntity<ApiResponse<RoutePlanResponse>> getRouteById(@PathVariable Long id) {
        log.info("GET /api/routes/{}", id);

        RoutePlan route = routeService.getRouteById(id);
        return ResponseEntity.ok(ApiResponse.success(
            "Route retrieved successfully", RoutePlanResponse.from(route)));
    }

    /**
     * Create a new route
     */
    @PostMapping
    @Operation(summary = "Create a new route plan")
    public ResponseEntity<ApiResponse<RoutePlanResponse>> createRoute(
            @Valid @RequestBody RoutePlanRequest request) {
        log.info("POST /api/routes - Creating route: {}", request.getRouteName());

        RoutePlan route = routeService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Route created successfully", RoutePlanResponse.from(route)));
    }

    /**
     * Update a route
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing route plan")
    public ResponseEntity<ApiResponse<RoutePlanResponse>> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RoutePlanRequest request) {
        log.info("PUT /api/routes/{}", id);

        RoutePlan route = routeService.updateRoute(id, request);
        return ResponseEntity.ok(ApiResponse.success(
            "Route updated successfully", RoutePlanResponse.from(route)));
    }

    /**
     * Delete a route
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a route plan")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(@PathVariable Long id) {
        log.info("DELETE /api/routes/{}", id);

        routeService.deleteRoute(id);
        return ResponseEntity.ok(ApiResponse.success("Route deleted successfully", null));
    }

    /**
     * Add waypoint to route
     */
    @PostMapping("/{id}/waypoints")
    @Operation(summary = "Add a waypoint to a route")
    public ResponseEntity<ApiResponse<WaypointResponse>> addWaypoint(
            @PathVariable Long id,
            @Valid @RequestBody WaypointRequest request) {
        log.info("POST /api/routes/{}/waypoints", id);

        RouteWaypoint waypoint = routeService.addWaypoint(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Waypoint added successfully", WaypointResponse.from(waypoint)));
    }

    /**
     * Start a route
     */
    @PostMapping("/{id}/start")
    @Operation(summary = "Start route execution")
    public ResponseEntity<ApiResponse<RoutePlanResponse>> startRoute(@PathVariable Long id) {
        log.info("POST /api/routes/{}/start", id);

        RoutePlan route = routeService.startRoute(id);
        return ResponseEntity.ok(ApiResponse.success(
            "Route started successfully", RoutePlanResponse.from(route)));
    }

    /**
     * Complete a route
     */
    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete route execution")
    public ResponseEntity<ApiResponse<RoutePlanResponse>> completeRoute(@PathVariable Long id) {
        log.info("POST /api/routes/{}/complete", id);

        RoutePlan route = routeService.completeRoute(id);
        return ResponseEntity.ok(ApiResponse.success(
            "Route completed successfully", RoutePlanResponse.from(route)));
    }

    /**
     * Cancel a route
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel route execution")
    public ResponseEntity<ApiResponse<RoutePlanResponse>> cancelRoute(@PathVariable Long id) {
        log.info("POST /api/routes/{}/cancel", id);

        RoutePlan route = routeService.cancelRoute(id);
        return ResponseEntity.ok(ApiResponse.success(
            "Route cancelled successfully", RoutePlanResponse.from(route)));
    }

    /**
     * Get waypoints for a route
     */
    @GetMapping("/{id}/waypoints")
    @Operation(summary = "Get all waypoints for a route")
    public ResponseEntity<ApiResponse<List<WaypointResponse>>> getWaypoints(@PathVariable Long id) {
        log.info("GET /api/routes/{}/waypoints", id);

        List<WaypointResponse> waypoints = routeService.getWaypointsByRoute(id)
            .stream()
            .map(WaypointResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
            "Waypoints retrieved successfully", waypoints));
    }
}
