package com.evfleet.charging.controller;

import com.evfleet.charging.dto.ChargingStationResponse;
import com.evfleet.charging.model.ChargingStation;
import com.evfleet.charging.service.ChargingStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Charging Station Controller
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/charging/stations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Charging - Stations", description = "Charging Station Management API")
public class ChargingStationController {

    private final ChargingStationService stationService;

    @PostMapping
    @Operation(summary = "Register new charging station")
    public ResponseEntity<ChargingStationResponse> createStation(@RequestBody ChargingStation station) {
        log.info("POST /api/v1/charging/stations - Creating station: {}", station.getName());
        ChargingStation created = stationService.createStation(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(ChargingStationResponse.from(created));
    }

    @GetMapping
    @Operation(summary = "Get all charging stations")
    public ResponseEntity<List<ChargingStationResponse>> getAllStations() {
        log.info("GET /api/v1/charging/stations");
        List<ChargingStationResponse> stations = stationService.getAllStations()
            .stream()
            .map(ChargingStationResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get station by ID")
    public ResponseEntity<ChargingStationResponse> getStation(@PathVariable Long id) {
        log.info("GET /api/v1/charging/stations/{}", id);
        ChargingStation station = stationService.getStationById(id);
        return ResponseEntity.ok(ChargingStationResponse.from(station));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available stations")
    public ResponseEntity<List<ChargingStationResponse>> getAvailableStations() {
        log.info("GET /api/v1/charging/stations/available");
        List<ChargingStationResponse> stations = stationService.getAvailableStations()
            .stream()
            .map(ChargingStationResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/nearby")
    @Operation(summary = "Get nearby stations")
    public ResponseEntity<List<ChargingStationResponse>> getNearbyStations(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/v1/charging/stations/nearby - lat: {}, lon: {}", latitude, longitude);
        List<ChargingStationResponse> stations = stationService.getNearbyStations(latitude, longitude, limit)
            .stream()
            .map(ChargingStationResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Charging Service is running");
    }
}
