package com.evfleet.fleet.controller;

import com.evfleet.fleet.model.RoutePlan;
import com.evfleet.fleet.model.RouteWaypoint;
import com.evfleet.fleet.service.RoutePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Route Optimization", description = "APIs for route planning and optimization")
public class RoutePlanController {

    private final RoutePlanService routePlanService;

    @PostMapping
    @Operation(summary = "Create a new route plan")
    public ResponseEntity<RoutePlan> createRoutePlan(@RequestBody RoutePlan routePlan) {
        try {
            RoutePlan created = routePlanService.createRoutePlan(routePlan);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating route plan", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get route plan by ID")
    public ResponseEntity<RoutePlan> getRoutePlan(@PathVariable Long id) {
        try {
            RoutePlan routePlan = routePlanService.getRoutePlanById(id);
            return ResponseEntity.ok(routePlan);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all route plans")
    public ResponseEntity<List<RoutePlan>> getAllRoutePlans() {
        List<RoutePlan> routePlans = routePlanService.getAllRoutePlans();
        return ResponseEntity.ok(routePlans);
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get route plans by vehicle")
    public ResponseEntity<List<RoutePlan>> getRoutePlansByVehicle(@PathVariable Long vehicleId) {
        List<RoutePlan> routePlans = routePlanService.getRoutePlansByVehicle(vehicleId);
        return ResponseEntity.ok(routePlans);
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get route plans by driver")
    public ResponseEntity<List<RoutePlan>> getRoutePlansByDriver(@PathVariable Long driverId) {
        List<RoutePlan> routePlans = routePlanService.getRoutePlansByDriver(driverId);
        return ResponseEntity.ok(routePlans);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get route plans by status")
    public ResponseEntity<List<RoutePlan>> getRoutePlansByStatus(@PathVariable String status) {
        List<RoutePlan> routePlans = routePlanService.getRoutePlansByStatus(status);
        return ResponseEntity.ok(routePlans);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active route plans (PLANNED or IN_PROGRESS)")
    public ResponseEntity<List<RoutePlan>> getActiveRoutes() {
        List<RoutePlan> routePlans = routePlanService.getActiveRoutes();
        return ResponseEntity.ok(routePlans);
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's route plans")
    public ResponseEntity<List<RoutePlan>> getTodaysRoutes() {
        List<RoutePlan> routePlans = routePlanService.getTodaysRoutes();
        return ResponseEntity.ok(routePlans);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a route plan")
    public ResponseEntity<RoutePlan> updateRoutePlan(@PathVariable Long id, @RequestBody RoutePlan routePlan) {
        try {
            RoutePlan updated = routePlanService.updateRoutePlan(id, routePlan);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a route plan")
    public ResponseEntity<Void> deleteRoutePlan(@PathVariable Long id) {
        try {
            routePlanService.deleteRoutePlan(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Waypoint endpoints
    @PostMapping("/{routePlanId}/waypoints")
    @Operation(summary = "Add a waypoint to route plan")
    public ResponseEntity<RouteWaypoint> addWaypoint(
            @PathVariable Long routePlanId,
            @RequestBody RouteWaypoint waypoint) {
        try {
            RouteWaypoint created = routePlanService.addWaypoint(routePlanId, waypoint);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            log.error("Error adding waypoint", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{routePlanId}/waypoints")
    @Operation(summary = "Get waypoints for a route plan")
    public ResponseEntity<List<RouteWaypoint>> getWaypoints(@PathVariable Long routePlanId) {
        List<RouteWaypoint> waypoints = routePlanService.getWaypoints(routePlanId);
        return ResponseEntity.ok(waypoints);
    }

    @PutMapping("/waypoints/{waypointId}")
    @Operation(summary = "Update a waypoint")
    public ResponseEntity<RouteWaypoint> updateWaypoint(
            @PathVariable Long waypointId,
            @RequestBody RouteWaypoint waypoint) {
        try {
            RouteWaypoint updated = routePlanService.updateWaypoint(waypointId, waypoint);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/waypoints/{waypointId}")
    @Operation(summary = "Delete a waypoint")
    public ResponseEntity<Void> deleteWaypoint(@PathVariable Long waypointId) {
        try {
            routePlanService.deleteWaypoint(waypointId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Route execution endpoints
    @PostMapping("/{id}/start")
    @Operation(summary = "Start a route plan")
    public ResponseEntity<RoutePlan> startRoute(@PathVariable Long id) {
        try {
            RoutePlan updated = routePlanService.startRoute(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete a route plan")
    public ResponseEntity<RoutePlan> completeRoute(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal actualDistance,
            @RequestParam(required = false) Integer actualDuration,
            @RequestParam(required = false) BigDecimal actualFuelConsumption,
            @RequestParam(required = false) BigDecimal actualCost) {
        try {
            RoutePlan updated = routePlanService.completeRoute(id, actualDistance, actualDuration, 
                                                                actualFuelConsumption, actualCost);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a route plan")
    public ResponseEntity<RoutePlan> cancelRoute(@PathVariable Long id) {
        try {
            RoutePlan updated = routePlanService.cancelRoute(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
