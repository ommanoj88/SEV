package com.evfleet.charging.controller;

import com.evfleet.charging.dto.ChargingSessionResponse;
import com.evfleet.charging.model.ChargingSession;
import com.evfleet.charging.service.ChargingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Charging Session Controller
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/charging/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Charging - Sessions", description = "Charging Session Management API")
public class ChargingSessionController {

    private final ChargingSessionService sessionService;

    @GetMapping
    @Operation(summary = "Get all charging sessions")
    public ResponseEntity<com.evfleet.common.dto.ApiResponse<List<ChargingSessionResponse>>> getAllSessions(
            @RequestParam(required = false) Long companyId) {
        log.info("GET /api/v1/charging/sessions - companyId: {}", companyId);
        List<ChargingSessionResponse> sessions;

        if (companyId != null) {
            sessions = sessionService.getSessionsByCompany(companyId)
                .stream()
                .map(ChargingSessionResponse::from)
                .collect(Collectors.toList());
        } else {
            // Return empty list for now - in production, would need pagination
            sessions = List.of();
        }

        return ResponseEntity.ok(com.evfleet.common.dto.ApiResponse.success(
            "Charging sessions retrieved successfully", sessions));
    }

    @PostMapping("/start")
    @Operation(summary = "Start charging session")
    public ResponseEntity<ChargingSessionResponse> startSession(
            @RequestParam Long vehicleId,
            @RequestParam Long stationId,
            @RequestParam Long companyId,
            @RequestParam(required = false) Double initialSoc) {
        log.info("POST /api/v1/charging/sessions/start - Vehicle: {}, Station: {}", vehicleId, stationId);
        ChargingSession session = sessionService.startSession(vehicleId, stationId, companyId, initialSoc);
        return ResponseEntity.status(HttpStatus.CREATED).body(ChargingSessionResponse.from(session));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete charging session")
    public ResponseEntity<ChargingSessionResponse> completeSession(
            @PathVariable Long id,
            @RequestParam BigDecimal energyConsumed,
            @RequestParam(required = false) Double finalSoc) {
        log.info("POST /api/v1/charging/sessions/{}/complete - Energy: {} kWh", id, energyConsumed);
        ChargingSession session = sessionService.completeSession(id, energyConsumed, finalSoc);
        return ResponseEntity.ok(ChargingSessionResponse.from(session));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session by ID")
    public ResponseEntity<ChargingSessionResponse> getSession(@PathVariable Long id) {
        log.info("GET /api/v1/charging/sessions/{}", id);
        ChargingSession session = sessionService.getSessionById(id);
        return ResponseEntity.ok(ChargingSessionResponse.from(session));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get sessions by vehicle")
    public ResponseEntity<List<ChargingSessionResponse>> getSessionsByVehicle(@PathVariable Long vehicleId) {
        log.info("GET /api/v1/charging/sessions/vehicle/{}", vehicleId);
        List<ChargingSessionResponse> sessions = sessionService.getSessionsByVehicle(vehicleId)
            .stream()
            .map(ChargingSessionResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get sessions by company")
    public ResponseEntity<List<ChargingSessionResponse>> getSessionsByCompany(@PathVariable Long companyId) {
        log.info("GET /api/v1/charging/sessions/company/{}", companyId);
        List<ChargingSessionResponse> sessions = sessionService.getSessionsByCompany(companyId)
            .stream()
            .map(ChargingSessionResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }
}
