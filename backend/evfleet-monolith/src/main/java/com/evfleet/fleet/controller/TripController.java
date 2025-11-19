package com.evfleet.fleet.controller;

import com.evfleet.fleet.dto.TripResponse;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.service.TripService;
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
 * Trip Controller
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/fleet/trips")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fleet - Trips", description = "Trip Management API")
public class TripController {

    private final TripService tripService;

    @PostMapping("/start")
    @Operation(summary = "Start a trip")
    public ResponseEntity<TripResponse> startTrip(
            @RequestParam Long vehicleId,
            @RequestParam(required = false) Long driverId,
            @RequestParam Double startLatitude,
            @RequestParam Double startLongitude) {
        log.info("POST /api/v1/fleet/trips/start - Vehicle: {}", vehicleId);
        Trip trip = tripService.startTrip(vehicleId, driverId, startLatitude, startLongitude);
        return ResponseEntity.status(HttpStatus.CREATED).body(TripResponse.from(trip));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete a trip")
    public ResponseEntity<TripResponse> completeTrip(
            @PathVariable Long id,
            @RequestParam Double endLatitude,
            @RequestParam Double endLongitude,
            @RequestParam Double distance,
            @RequestParam(required = false) BigDecimal energyConsumed,
            @RequestParam(required = false) BigDecimal fuelConsumed) {
        log.info("POST /api/v1/fleet/trips/{}/complete", id);

        if (energyConsumed == null) {
            energyConsumed = BigDecimal.ZERO;
        }
        
        if (fuelConsumed == null) {
            fuelConsumed = BigDecimal.ZERO;
        }

        Trip trip = tripService.completeTrip(id, endLatitude, endLongitude, distance, energyConsumed, fuelConsumed);
        return ResponseEntity.ok(TripResponse.from(trip));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trip by ID")
    public ResponseEntity<TripResponse> getTrip(@PathVariable Long id) {
        log.info("GET /api/v1/fleet/trips/{}", id);
        Trip trip = tripService.getTripById(id);
        return ResponseEntity.ok(TripResponse.from(trip));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get trips by vehicle")
    public ResponseEntity<List<TripResponse>> getTripsByVehicle(@PathVariable Long vehicleId) {
        log.info("GET /api/v1/fleet/trips/vehicle/{}", vehicleId);
        List<TripResponse> trips = tripService.getTripsByVehicle(vehicleId)
            .stream()
            .map(TripResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get trips by company")
    public ResponseEntity<List<TripResponse>> getTripsByCompany(@PathVariable Long companyId) {
        log.info("GET /api/v1/fleet/trips/company/{}", companyId);
        List<TripResponse> trips = tripService.getTripsByCompany(companyId)
            .stream()
            .map(TripResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(trips);
    }
}
