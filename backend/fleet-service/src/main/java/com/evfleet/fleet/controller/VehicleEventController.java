package com.evfleet.fleet.controller;

import com.evfleet.fleet.dto.VehicleCurrentStateResponse;
import com.evfleet.fleet.dto.VehicleEventResponse;
import com.evfleet.fleet.model.VehicleEvent;
import com.evfleet.fleet.service.VehicleCurrentStateService;
import com.evfleet.fleet.service.VehicleEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Vehicle Events and Current State
 * Provides endpoints for event history and real-time vehicle state
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicle Events & State", description = "APIs for vehicle event history and current state")
public class VehicleEventController {

    private final VehicleEventService vehicleEventService;
    private final VehicleCurrentStateService currentStateService;

    // ========================================
    // VEHICLE EVENTS ENDPOINTS
    // ========================================

    @GetMapping("/{vehicleId}/events")
    @Operation(summary = "Get vehicle events", 
               description = "Retrieve paginated event history for a specific vehicle")
    public ResponseEntity<Page<VehicleEventResponse>> getVehicleEvents(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get events for vehicle ID: {}", vehicleId);
        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleEventResponse> events = vehicleEventService.getVehicleEvents(vehicleId, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{vehicleId}/events/type/{eventType}")
    @Operation(summary = "Get vehicle events by type", 
               description = "Retrieve events of a specific type for a vehicle")
    public ResponseEntity<Page<VehicleEventResponse>> getVehicleEventsByType(
            @PathVariable Long vehicleId,
            @PathVariable VehicleEvent.EventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get events of type {} for vehicle ID: {}", eventType, vehicleId);
        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleEventResponse> events = vehicleEventService.getVehicleEventsByType(
                vehicleId, eventType, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{vehicleId}/events/range")
    @Operation(summary = "Get vehicle events by time range", 
               description = "Retrieve events within a specific time range")
    public ResponseEntity<List<VehicleEventResponse>> getVehicleEventsByTimeRange(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("REST request to get events for vehicle ID: {} between {} and {}", 
                vehicleId, startTime, endTime);
        List<VehicleEventResponse> events = vehicleEventService.getVehicleEventsByTimeRange(
                vehicleId, startTime, endTime);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{vehicleId}/events/critical")
    @Operation(summary = "Get critical events", 
               description = "Retrieve high severity and critical events for a vehicle")
    public ResponseEntity<List<VehicleEventResponse>> getCriticalEvents(@PathVariable Long vehicleId) {
        log.info("REST request to get critical events for vehicle ID: {}", vehicleId);
        List<VehicleEventResponse> events = vehicleEventService.getCriticalEvents(vehicleId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{vehicleId}/events/recent")
    @Operation(summary = "Get recent events", 
               description = "Retrieve events from the last N days")
    public ResponseEntity<List<VehicleEventResponse>> getRecentEvents(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "7") int days) {
        log.info("REST request to get recent events (last {} days) for vehicle ID: {}", days, vehicleId);
        List<VehicleEventResponse> events = vehicleEventService.getRecentEvents(vehicleId, days);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{vehicleId}/events/count")
    @Operation(summary = "Get event count by type", 
               description = "Get the count of events of a specific type")
    public ResponseEntity<Long> getEventCountByType(
            @PathVariable Long vehicleId,
            @RequestParam VehicleEvent.EventType eventType) {
        log.info("REST request to get event count of type {} for vehicle ID: {}", eventType, vehicleId);
        Long count = vehicleEventService.getEventCountByType(vehicleId, eventType);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/company/{companyId}/events")
    @Operation(summary = "Get company events", 
               description = "Retrieve paginated event history for all vehicles in a company")
    public ResponseEntity<Page<VehicleEventResponse>> getCompanyEvents(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get events for company ID: {}", companyId);
        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleEventResponse> events = vehicleEventService.getCompanyEvents(companyId, pageable);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/{vehicleId}/events")
    @Operation(summary = "Record a vehicle event", 
               description = "Create a new event record for a vehicle")
    public ResponseEntity<VehicleEventResponse> recordEvent(
            @PathVariable Long vehicleId,
            @RequestBody VehicleEvent event) {
        log.info("REST request to record event for vehicle ID: {}", vehicleId);
        event.setVehicleId(vehicleId);
        VehicleEventResponse response = vehicleEventService.recordEvent(event);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ========================================
    // VEHICLE CURRENT STATE ENDPOINTS
    // ========================================

    @GetMapping("/{vehicleId}/current-state")
    @Operation(summary = "Get vehicle current state", 
               description = "Retrieve the current state snapshot of a vehicle")
    public ResponseEntity<VehicleCurrentStateResponse> getVehicleCurrentState(
            @PathVariable Long vehicleId) {
        log.info("REST request to get current state for vehicle ID: {}", vehicleId);
        VehicleCurrentStateResponse state = currentStateService.getVehicleCurrentState(vehicleId);
        return ResponseEntity.ok(state);
    }

    @GetMapping("/company/{companyId}/current-states")
    @Operation(summary = "Get company vehicle states", 
               description = "Retrieve current states for all vehicles in a company")
    public ResponseEntity<List<VehicleCurrentStateResponse>> getCompanyVehicleStates(
            @PathVariable Long companyId) {
        log.info("REST request to get vehicle states for company ID: {}", companyId);
        List<VehicleCurrentStateResponse> states = currentStateService.getCompanyVehicleStates(companyId);
        return ResponseEntity.ok(states);
    }

    @GetMapping("/current-states/charging")
    @Operation(summary = "Get charging vehicles", 
               description = "Retrieve all vehicles currently charging")
    public ResponseEntity<List<VehicleCurrentStateResponse>> getChargingVehicles() {
        log.info("REST request to get vehicles currently charging");
        List<VehicleCurrentStateResponse> states = currentStateService.getChargingVehicles();
        return ResponseEntity.ok(states);
    }

    @GetMapping("/current-states/maintenance")
    @Operation(summary = "Get vehicles in maintenance", 
               description = "Retrieve all vehicles currently in maintenance")
    public ResponseEntity<List<VehicleCurrentStateResponse>> getMaintenanceVehicles() {
        log.info("REST request to get vehicles in maintenance");
        List<VehicleCurrentStateResponse> states = currentStateService.getMaintenanceVehicles();
        return ResponseEntity.ok(states);
    }

    @GetMapping("/current-states/in-trip")
    @Operation(summary = "Get vehicles in trip", 
               description = "Retrieve all vehicles currently in a trip")
    public ResponseEntity<List<VehicleCurrentStateResponse>> getVehiclesInTrip() {
        log.info("REST request to get vehicles in trip");
        List<VehicleCurrentStateResponse> states = currentStateService.getVehiclesInTrip();
        return ResponseEntity.ok(states);
    }

    @GetMapping("/current-states/with-alerts")
    @Operation(summary = "Get vehicles with alerts", 
               description = "Retrieve all vehicles with active alerts")
    public ResponseEntity<List<VehicleCurrentStateResponse>> getVehiclesWithAlerts() {
        log.info("REST request to get vehicles with alerts");
        List<VehicleCurrentStateResponse> states = currentStateService.getVehiclesWithAlerts();
        return ResponseEntity.ok(states);
    }

    @GetMapping("/current-states/critical-alerts")
    @Operation(summary = "Get vehicles with critical alerts", 
               description = "Retrieve all vehicles with critical alerts")
    public ResponseEntity<List<VehicleCurrentStateResponse>> getVehiclesWithCriticalAlerts() {
        log.info("REST request to get vehicles with critical alerts");
        List<VehicleCurrentStateResponse> states = currentStateService.getVehiclesWithCriticalAlerts();
        return ResponseEntity.ok(states);
    }

    @GetMapping("/current-states/disconnected")
    @Operation(summary = "Get disconnected vehicles", 
               description = "Retrieve all offline/disconnected vehicles")
    public ResponseEntity<List<VehicleCurrentStateResponse>> getDisconnectedVehicles() {
        log.info("REST request to get disconnected vehicles");
        List<VehicleCurrentStateResponse> states = currentStateService.getDisconnectedVehicles();
        return ResponseEntity.ok(states);
    }
}
