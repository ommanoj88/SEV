package com.evfleet.charging.controller;

import com.evfleet.charging.dto.ChargingSessionRequest;
import com.evfleet.charging.dto.ChargingSessionResponse;
import com.evfleet.charging.service.ChargingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Charging Session operations
 */
@RestController
@RequestMapping("/api/v1/charging/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Charging Session Management", description = "APIs for managing charging sessions")
public class ChargingSessionController {

    private final ChargingSessionService chargingSessionService;

    @PostMapping
    @Operation(summary = "Start a charging session", description = "Start a new charging session for a vehicle")
    public ResponseEntity<ChargingSessionResponse> startSession(@Valid @RequestBody ChargingSessionRequest request) {
        log.info("REST request to start charging session for vehicle ID: {}", request.getVehicleId());
        ChargingSessionResponse response = chargingSessionService.startSession(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/end")
    @Operation(summary = "End a charging session", description = "End an active charging session")
    public ResponseEntity<ChargingSessionResponse> endSession(
            @PathVariable Long id,
            @RequestParam BigDecimal endBatteryLevel) {
        log.info("REST request to end charging session ID: {}", id);
        ChargingSessionResponse response = chargingSessionService.endSession(id, endBatteryLevel);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get charging session by ID", description = "Retrieve charging session details by ID")
    public ResponseEntity<ChargingSessionResponse> getSessionById(@PathVariable Long id) {
        log.info("REST request to get charging session ID: {}", id);
        ChargingSessionResponse response = chargingSessionService.getSessionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get sessions by vehicle", description = "Retrieve all charging sessions for a vehicle")
    public ResponseEntity<List<ChargingSessionResponse>> getSessionsByVehicle(@PathVariable Long vehicleId) {
        log.info("REST request to get charging sessions for vehicle ID: {}", vehicleId);
        List<ChargingSessionResponse> response = chargingSessionService.getSessionsByVehicle(vehicleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/station/{stationId}")
    @Operation(summary = "Get sessions by station", description = "Retrieve all charging sessions for a station")
    public ResponseEntity<List<ChargingSessionResponse>> getSessionsByStation(@PathVariable Long stationId) {
        log.info("REST request to get charging sessions for station ID: {}", stationId);
        List<ChargingSessionResponse> response = chargingSessionService.getSessionsByStation(stationId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a charging session", description = "Cancel an active charging session")
    public ResponseEntity<ChargingSessionResponse> cancelSession(@PathVariable Long id) {
        log.info("REST request to cancel charging session ID: {}", id);
        ChargingSessionResponse response = chargingSessionService.cancelSession(id);
        return ResponseEntity.ok(response);
    }
}
