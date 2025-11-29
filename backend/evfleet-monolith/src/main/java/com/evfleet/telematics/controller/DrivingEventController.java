package com.evfleet.telematics.controller;

import com.evfleet.telematics.dto.DrivingEventRequest;
import com.evfleet.telematics.dto.DrivingEventResponse;
import com.evfleet.telematics.model.DrivingEvent;
import com.evfleet.telematics.service.DrivingEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.util.Map;

/**
 * REST Controller for Driving Events
 * 
 * Provides endpoints for:
 * - Recording driving events from telematics
 * - Querying events by trip, driver, or vehicle
 * - Getting driver safety scores
 * - Getting event breakdowns
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/driving-events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Driving Events", description = "Driver behavior monitoring and telematics event management")
public class DrivingEventController {

    private final DrivingEventService drivingEventService;

    @PostMapping
    @Operation(
        summary = "Record a driving event",
        description = "Records a new driving event from telematics data. Events are used to calculate driver safety scores."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Event recorded successfully",
            content = @Content(schema = @Schema(implementation = DrivingEventResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid event data"),
        @ApiResponse(responseCode = "404", description = "Driver or vehicle not found")
    })
    public ResponseEntity<DrivingEventResponse> recordEvent(
            @Valid @RequestBody DrivingEventRequest request) {
        log.info("POST /api/v1/driving-events - Recording event type: {} for driver: {}", 
            request.getEventType(), request.getDriverId());
        DrivingEventResponse response = drivingEventService.recordEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/trip/{tripId}")
    @Operation(
        summary = "Get events by trip",
        description = "Retrieves all driving events recorded during a specific trip"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trip not found")
    })
    public ResponseEntity<List<DrivingEventResponse>> getEventsByTrip(
            @Parameter(description = "Trip ID") @PathVariable Long tripId) {
        log.info("GET /api/v1/driving-events/trip/{} - Fetching events", tripId);
        List<DrivingEventResponse> events = drivingEventService.getEventsByTrip(tripId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/driver/{driverId}")
    @Operation(
        summary = "Get events by driver",
        description = "Retrieves driving events for a specific driver within an optional date range. Defaults to last 30 days if no dates specified."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    public ResponseEntity<List<DrivingEventResponse>> getEventsByDriver(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/v1/driving-events/driver/{} - Fetching events from {} to {}", driverId, start, end);
        List<DrivingEventResponse> events = drivingEventService.getEventsByDriver(driverId, start, end);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(
        summary = "Get events by vehicle",
        description = "Retrieves all driving events recorded for a specific vehicle"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<List<DrivingEventResponse>> getEventsByVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long vehicleId) {
        log.info("GET /api/v1/driving-events/vehicle/{} - Fetching events", vehicleId);
        List<DrivingEventResponse> events = drivingEventService.getEventsByVehicle(vehicleId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/driver/{driverId}/safety-score")
    @Operation(
        summary = "Get driver safety score",
        description = "Calculates and returns the driver's safety score based on recent driving events. Score ranges from 0-100."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Safety score calculated successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    public ResponseEntity<Map<String, Object>> getDriverSafetyScore(
            @Parameter(description = "Driver ID") @PathVariable Long driverId) {
        log.info("GET /api/v1/driving-events/driver/{}/safety-score - Calculating score", driverId);
        double score = drivingEventService.calculateDriverSafetyScore(driverId);
        return ResponseEntity.ok(Map.of(
            "driverId", driverId,
            "safetyScore", score,
            "rating", getSafetyRating(score),
            "calculatedAt", LocalDateTime.now()
        ));
    }

    @GetMapping("/driver/{driverId}/breakdown")
    @Operation(
        summary = "Get event breakdown",
        description = "Returns a count of events by type for a driver over the last 30 days"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Breakdown retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    public ResponseEntity<Map<String, Object>> getEventBreakdown(
            @Parameter(description = "Driver ID") @PathVariable Long driverId) {
        log.info("GET /api/v1/driving-events/driver/{}/breakdown - Fetching event breakdown", driverId);
        Map<DrivingEvent.EventType, Long> breakdown = drivingEventService.getEventBreakdown(driverId);
        double safetyScore = drivingEventService.calculateDriverSafetyScore(driverId);
        
        return ResponseEntity.ok(Map.of(
            "driverId", driverId,
            "events", breakdown,
            "totalEvents", breakdown.values().stream().mapToLong(Long::longValue).sum(),
            "safetyScore", safetyScore,
            "rating", getSafetyRating(safetyScore),
            "period", "last_30_days"
        ));
    }

    @GetMapping("/driver/{driverId}/count")
    @Operation(
        summary = "Get event count by type",
        description = "Returns the count of a specific event type for a driver within a date range"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getEventCount(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Parameter(description = "Event type") @RequestParam DrivingEvent.EventType eventType,
            @Parameter(description = "Start date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "End date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/v1/driving-events/driver/{}/count - Type: {}, {} to {}", 
            driverId, eventType, start, end);
        long count = drivingEventService.getEventCountByType(driverId, eventType, start, end);
        return ResponseEntity.ok(Map.of(
            "driverId", driverId,
            "eventType", eventType,
            "count", count,
            "startDate", start,
            "endDate", end
        ));
    }

    /**
     * Get safety rating label based on score.
     */
    private String getSafetyRating(double score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 75) return "GOOD";
        if (score >= 60) return "FAIR";
        if (score >= 40) return "POOR";
        return "CRITICAL";
    }
}
