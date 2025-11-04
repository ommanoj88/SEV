package com.evfleet.charging.controller;

import com.evfleet.charging.dto.ChargingStationRequest;
import com.evfleet.charging.dto.ChargingStationResponse;
import com.evfleet.charging.service.ChargingStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Charging Station operations
 */
@RestController
@RequestMapping("/api/v1/charging/stations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Charging Station Management", description = "APIs for managing charging stations")
public class ChargingStationController {

    private final ChargingStationService chargingStationService;

    @PostMapping
    @Operation(summary = "Create a new charging station", description = "Add a new charging station to the network")
    public ResponseEntity<ChargingStationResponse> createStation(@Valid @RequestBody ChargingStationRequest request) {
        log.info("REST request to create charging station: {}", request.getName());
        ChargingStationResponse response = chargingStationService.createStation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a charging station", description = "Update an existing charging station's information")
    public ResponseEntity<ChargingStationResponse> updateStation(
            @PathVariable Long id,
            @Valid @RequestBody ChargingStationRequest request) {
        log.info("REST request to update charging station ID: {}", id);
        ChargingStationResponse response = chargingStationService.updateStation(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a charging station", description = "Remove a charging station from the network")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        log.info("REST request to delete charging station ID: {}", id);
        chargingStationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get charging station by ID", description = "Retrieve charging station details by ID")
    public ResponseEntity<ChargingStationResponse> getStationById(@PathVariable Long id) {
        log.info("REST request to get charging station ID: {}", id);
        ChargingStationResponse response = chargingStationService.getStationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all charging stations", description = "Retrieve all charging stations in the system")
    public ResponseEntity<List<ChargingStationResponse>> getAllStations() {
        log.info("REST request to get all charging stations");
        List<ChargingStationResponse> response = chargingStationService.getAllStations();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available charging stations", description = "Retrieve all available charging stations")
    public ResponseEntity<List<ChargingStationResponse>> getAvailableStations() {
        log.info("REST request to get available charging stations");
        List<ChargingStationResponse> response = chargingStationService.getAvailableStations();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nearest")
    @Operation(summary = "Get nearest charging stations", description = "Retrieve nearest charging stations to given location")
    public ResponseEntity<List<ChargingStationResponse>> getNearestStations(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("REST request to get nearest stations to location: {}, {}", latitude, longitude);
        List<ChargingStationResponse> response = chargingStationService.getNearestStations(latitude, longitude, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/provider/{provider}")
    @Operation(summary = "Get stations by provider", description = "Retrieve charging stations by provider name")
    public ResponseEntity<List<ChargingStationResponse>> getStationsByProvider(@PathVariable String provider) {
        log.info("REST request to get stations by provider: {}", provider);
        List<ChargingStationResponse> response = chargingStationService.getStationsByProvider(provider);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reserve")
    @Operation(summary = "Reserve a slot", description = "Reserve a charging slot at a station")
    public ResponseEntity<Void> reserveSlot(@PathVariable Long id) {
        log.info("REST request to reserve slot at station ID: {}", id);
        chargingStationService.reserveSlot(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release a slot", description = "Release a charging slot at a station")
    public ResponseEntity<Void> releaseSlot(@PathVariable Long id) {
        log.info("REST request to release slot at station ID: {}", id);
        chargingStationService.releaseSlot(id);
        return ResponseEntity.ok().build();
    }
}
