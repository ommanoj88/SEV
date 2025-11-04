package com.evfleet.fleet.controller;

import com.evfleet.fleet.dto.TripRequest;
import com.evfleet.fleet.dto.TripResponse;
import com.evfleet.fleet.service.TripService;
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
 * REST Controller for Trip operations
 */
@RestController
@RequestMapping("/api/fleet/trips")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trip Management", description = "APIs for managing vehicle trips")
public class TripController {

    private final TripService tripService;

    @PostMapping
    @Operation(summary = "Start a new trip", description = "Initiate a new trip for a vehicle")
    public ResponseEntity<TripResponse> startTrip(@Valid @RequestBody TripRequest request) {
        log.info("REST request to start trip for vehicle ID: {}", request.getVehicleId());
        TripResponse response = tripService.startTrip(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/end")
    @Operation(summary = "End a trip", description = "Complete an ongoing trip")
    public ResponseEntity<TripResponse> endTrip(
            @PathVariable Long id,
            @RequestParam String endLocation) {
        log.info("REST request to end trip ID: {}", id);
        TripResponse response = tripService.endTrip(id, endLocation);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/pause")
    @Operation(summary = "Pause a trip", description = "Temporarily pause an ongoing trip")
    public ResponseEntity<TripResponse> pauseTrip(@PathVariable Long id) {
        log.info("REST request to pause trip ID: {}", id);
        TripResponse response = tripService.pauseTrip(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/resume")
    @Operation(summary = "Resume a trip", description = "Resume a paused trip")
    public ResponseEntity<TripResponse> resumeTrip(@PathVariable Long id) {
        log.info("REST request to resume trip ID: {}", id);
        TripResponse response = tripService.resumeTrip(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a trip", description = "Cancel an ongoing or paused trip")
    public ResponseEntity<Void> cancelTrip(@PathVariable Long id) {
        log.info("REST request to cancel trip ID: {}", id);
        tripService.cancelTrip(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trip by ID", description = "Retrieve trip details by ID")
    public ResponseEntity<TripResponse> getTripById(@PathVariable Long id) {
        log.info("REST request to get trip ID: {}", id);
        TripResponse response = tripService.getTripById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get trips by vehicle", description = "Retrieve all trips for a specific vehicle")
    public ResponseEntity<List<TripResponse>> getTripsByVehicle(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "50") int limit) {
        log.info("REST request to get trips for vehicle ID: {}", vehicleId);
        List<TripResponse> response = tripService.getTripsByVehicle(vehicleId, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get trips by driver", description = "Retrieve all trips for a specific driver")
    public ResponseEntity<List<TripResponse>> getTripsByDriver(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "50") int limit) {
        log.info("REST request to get trips for driver ID: {}", driverId);
        List<TripResponse> response = tripService.getTripsByDriver(driverId, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get trips by company", description = "Retrieve all trips for a specific company")
    public ResponseEntity<List<TripResponse>> getTripsByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "100") int limit) {
        log.info("REST request to get trips for company ID: {}", companyId);
        List<TripResponse> response = tripService.getTripsByCompany(companyId, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}/ongoing")
    @Operation(summary = "Get ongoing trips", description = "Retrieve all currently ongoing trips for a company")
    public ResponseEntity<List<TripResponse>> getOngoingTrips(@PathVariable Long companyId) {
        log.info("REST request to get ongoing trips for company ID: {}", companyId);
        List<TripResponse> response = tripService.getOngoingTrips(companyId);
        return ResponseEntity.ok(response);
    }
}
