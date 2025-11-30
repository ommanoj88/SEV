package com.evfleet.telematics.controller;

import com.evfleet.common.dto.ApiResponse;
import com.evfleet.telematics.dto.DrivingEventResponse;
import com.evfleet.telematics.dto.TelematicsEventRequest;
import com.evfleet.telematics.service.TelematicsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Telematics Controller
 * Handles telematics event ingestion and retrieval
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/telematics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Telematics", description = "Telematics and Driver Behavior Monitoring API")
public class TelematicsController {

    private final TelematicsService telematicsService;

    @PostMapping("/events")
    @Operation(summary = "Ingest telematics event from vehicle sensors")
    public ResponseEntity<ApiResponse<DrivingEventResponse>> ingestEvent(
            @Valid @RequestBody TelematicsEventRequest request) {
        log.info("POST /api/v1/telematics/events - type: {}, vehicleId: {}", 
            request.getType(), request.getVehicleId());
        
        DrivingEventResponse event = telematicsService.ingestEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event ingested successfully", event));
    }

    @GetMapping("/events/trip/{tripId}")
    @Operation(summary = "Get all events for a specific trip")
    public ResponseEntity<ApiResponse<List<DrivingEventResponse>>> getEventsByTrip(
            @PathVariable Long tripId) {
        log.info("GET /api/v1/telematics/events/trip/{}", tripId);
        
        List<DrivingEventResponse> events = telematicsService.getEventsByTrip(tripId);
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
    }

    @GetMapping("/events/driver/{driverId}")
    @Operation(summary = "Get all events for a specific driver")
    public ResponseEntity<ApiResponse<List<DrivingEventResponse>>> getEventsByDriver(
            @PathVariable Long driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/v1/telematics/events/driver/{} - start: {}, end: {}", driverId, start, end);
        
        List<DrivingEventResponse> events = telematicsService.getEventsByDriver(driverId, start, end);
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
    }

    @GetMapping("/events/vehicle/{vehicleId}")
    @Operation(summary = "Get all events for a specific vehicle")
    public ResponseEntity<ApiResponse<List<DrivingEventResponse>>> getEventsByVehicle(
            @PathVariable Long vehicleId) {
        log.info("GET /api/v1/telematics/events/vehicle/{}", vehicleId);
        
        List<DrivingEventResponse> events = telematicsService.getEventsByVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
    }

    @GetMapping("/stats/driver/{driverId}")
    @Operation(summary = "Get event statistics for a driver")
    public ResponseEntity<ApiResponse<TelematicsService.DriverEventStats>> getDriverStats(
            @PathVariable Long driverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/v1/telematics/stats/driver/{} - start: {}, end: {}", driverId, start, end);
        
        TelematicsService.DriverEventStats stats = telematicsService.getDriverEventStats(driverId, start, end);
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
    }

    @GetMapping("/health")
    @Operation(summary = "Get telematics provider health status")
    public ResponseEntity<ApiResponse<Object>> getHealthStatus() {
        log.info("GET /api/v1/telematics/health");
        
        Object healthStatus = telematicsService.getProviderHealthStatus();
        return ResponseEntity.ok(ApiResponse.success("Health status retrieved", healthStatus));
    }

    @GetMapping("/providers")
    @Operation(summary = "List all available telematics providers")
    public ResponseEntity<ApiResponse<List<String>>> listProviders() {
        log.info("GET /api/v1/telematics/providers");
        
        List<String> providers = telematicsService.getAvailableProviders();
        return ResponseEntity.ok(ApiResponse.success("Providers listed", providers));
    }

    @PostMapping("/providers/{providerId}/test")
    @Operation(summary = "Test connection to a specific telematics provider")
    public ResponseEntity<ApiResponse<Boolean>> testProviderConnection(@PathVariable String providerId) {
        log.info("POST /api/v1/telematics/providers/{}/test", providerId);
        
        boolean connected = telematicsService.testProviderConnection(providerId);
        String message = connected ? "Provider connection successful" : "Provider connection failed";
        return ResponseEntity.ok(ApiResponse.success(message, connected));
    }
}
