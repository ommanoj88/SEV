package com.evfleet.fleet.controller;

import com.evfleet.fleet.dto.TelemetryRequest;
import com.evfleet.fleet.dto.TelemetryResponse;
import com.evfleet.fleet.service.TelemetryService;
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
 * REST Controller for Telemetry operations
 */
@RestController
@RequestMapping("/api/fleet/telemetry")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Telemetry Management", description = "APIs for handling vehicle telemetry data")
public class TelemetryController {

    private final TelemetryService telemetryService;

    @PostMapping
    @Operation(summary = "Submit telemetry data", description = "Submit real-time telemetry data from a vehicle")
    public ResponseEntity<TelemetryResponse> submitTelemetry(@Valid @RequestBody TelemetryRequest request) {
        log.debug("REST request to submit telemetry for vehicle ID: {}", request.getVehicleId());
        TelemetryResponse response = telemetryService.processTelemetryData(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    @Operation(summary = "Submit batch telemetry data", description = "Submit multiple telemetry records in a batch")
    public ResponseEntity<List<TelemetryResponse>> submitTelemetryBatch(
            @Valid @RequestBody List<TelemetryRequest> requests) {
        log.debug("REST request to submit batch telemetry with {} records", requests.size());
        List<TelemetryResponse> responses = requests.stream()
                .map(telemetryService::processTelemetryData)
                .toList();
        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get telemetry by vehicle", description = "Retrieve telemetry data for a specific vehicle")
    public ResponseEntity<List<TelemetryResponse>> getTelemetryByVehicle(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "100") int limit) {
        log.debug("REST request to get telemetry for vehicle ID: {}", vehicleId);
        List<TelemetryResponse> response = telemetryService.getTelemetryByVehicle(vehicleId, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicle/{vehicleId}/range")
    @Operation(summary = "Get telemetry by time range", description = "Retrieve telemetry data within a time range")
    public ResponseEntity<List<TelemetryResponse>> getTelemetryByTimeRange(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.debug("REST request to get telemetry for vehicle ID: {} from {} to {}", vehicleId, startTime, endTime);
        List<TelemetryResponse> response = telemetryService.getTelemetryByVehicleAndTimeRange(vehicleId, startTime, endTime);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicle/{vehicleId}/latest")
    @Operation(summary = "Get latest telemetry", description = "Retrieve the most recent telemetry data for a vehicle")
    public ResponseEntity<TelemetryResponse> getLatestTelemetry(@PathVariable Long vehicleId) {
        log.debug("REST request to get latest telemetry for vehicle ID: {}", vehicleId);
        TelemetryResponse response = telemetryService.getLatestTelemetry(vehicleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trip/{tripId}")
    @Operation(summary = "Get telemetry by trip", description = "Retrieve all telemetry data for a specific trip")
    public ResponseEntity<List<TelemetryResponse>> getTelemetryByTrip(@PathVariable Long tripId) {
        log.debug("REST request to get telemetry for trip ID: {}", tripId);
        List<TelemetryResponse> response = telemetryService.getTelemetryByTrip(tripId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicle/{vehicleId}/errors")
    @Operation(summary = "Get telemetry with errors", description = "Retrieve telemetry records that contain error codes")
    public ResponseEntity<List<TelemetryResponse>> getTelemetryWithErrors(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "50") int limit) {
        log.debug("REST request to get telemetry with errors for vehicle ID: {}", vehicleId);
        List<TelemetryResponse> response = telemetryService.getTelemetryWithErrors(vehicleId, limit);
        return ResponseEntity.ok(response);
    }
}
