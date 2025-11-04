package com.evfleet.fleet.controller;

import com.evfleet.fleet.dto.GeofenceRequest;
import com.evfleet.fleet.dto.GeofenceResponse;
import com.evfleet.fleet.model.Geofence;
import com.evfleet.fleet.service.GeofenceService;
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
 * REST Controller for Geofence operations
 */
@RestController
@RequestMapping("/api/fleet/geofences")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Geofence Management", description = "APIs for managing geofences")
public class GeofenceController {

    private final GeofenceService geofenceService;

    @PostMapping
    @Operation(summary = "Create a new geofence", description = "Create a geographic boundary for monitoring")
    public ResponseEntity<GeofenceResponse> createGeofence(@Valid @RequestBody GeofenceRequest request) {
        log.info("REST request to create geofence: {}", request.getName());
        GeofenceResponse response = geofenceService.createGeofence(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a geofence", description = "Update an existing geofence")
    public ResponseEntity<GeofenceResponse> updateGeofence(
            @PathVariable Long id,
            @Valid @RequestBody GeofenceRequest request) {
        log.info("REST request to update geofence ID: {}", id);
        GeofenceResponse response = geofenceService.updateGeofence(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a geofence", description = "Remove a geofence")
    public ResponseEntity<Void> deleteGeofence(@PathVariable Long id) {
        log.info("REST request to delete geofence ID: {}", id);
        geofenceService.deleteGeofence(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get geofence by ID", description = "Retrieve geofence details by ID")
    public ResponseEntity<GeofenceResponse> getGeofenceById(@PathVariable Long id) {
        log.info("REST request to get geofence ID: {}", id);
        GeofenceResponse response = geofenceService.getGeofenceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get geofences by company", description = "Retrieve all geofences for a company")
    public ResponseEntity<List<GeofenceResponse>> getGeofencesByCompany(@PathVariable Long companyId) {
        log.info("REST request to get geofences for company ID: {}", companyId);
        List<GeofenceResponse> response = geofenceService.getGeofencesByCompany(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}/active")
    @Operation(summary = "Get active geofences", description = "Retrieve all active geofences for a company")
    public ResponseEntity<List<GeofenceResponse>> getActiveGeofences(@PathVariable Long companyId) {
        log.info("REST request to get active geofences for company ID: {}", companyId);
        List<GeofenceResponse> response = geofenceService.getActiveGeofences(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}/type/{type}")
    @Operation(summary = "Get geofences by type", description = "Retrieve geofences filtered by type")
    public ResponseEntity<List<GeofenceResponse>> getGeofencesByType(
            @PathVariable Long companyId,
            @PathVariable Geofence.GeofenceType type) {
        log.info("REST request to get geofences of type {} for company ID: {}", type, companyId);
        List<GeofenceResponse> response = geofenceService.getGeofencesByType(companyId, type);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle geofence status", description = "Activate or deactivate a geofence")
    public ResponseEntity<Void> toggleGeofence(
            @PathVariable Long id,
            @RequestParam Boolean active) {
        log.info("REST request to toggle geofence ID: {} to active: {}", id, active);
        geofenceService.toggleGeofence(id, active);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/check")
    @Operation(summary = "Check if point is in geofence", description = "Verify if a coordinate is inside a geofence")
    public ResponseEntity<Boolean> checkPointInGeofence(
            @PathVariable Long id,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        log.info("REST request to check if point ({}, {}) is in geofence ID: {}", latitude, longitude, id);
        boolean isInside = geofenceService.isPointInGeofence(id, latitude, longitude);
        return ResponseEntity.ok(isInside);
    }

    @GetMapping("/company/{companyId}/containing-point")
    @Operation(summary = "Get geofences containing point", description = "Find all geofences that contain a specific point")
    public ResponseEntity<List<GeofenceResponse>> getGeofencesContainingPoint(
            @PathVariable Long companyId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        log.info("REST request to find geofences containing point ({}, {}) for company ID: {}", latitude, longitude, companyId);
        List<GeofenceResponse> response = geofenceService.getGeofencesContainingPoint(companyId, latitude, longitude);
        return ResponseEntity.ok(response);
    }
}
