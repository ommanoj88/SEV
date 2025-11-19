package com.evfleet.geofencing.controller;

import com.evfleet.common.dto.ApiResponse;
import com.evfleet.geofencing.dto.GeofenceRequest;
import com.evfleet.geofencing.dto.GeofenceResponse;
import com.evfleet.geofencing.model.Geofence;
import com.evfleet.geofencing.service.GeofenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Geofencing Controller
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/geofences")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Geofencing", description = "Geofencing & Spatial Alerts API")
public class GeofenceController {

    private final GeofenceService geofenceService;

    /**
     * Get all geofences
     */
    @GetMapping
    @Operation(summary = "Get all geofences for a company")
    public ResponseEntity<ApiResponse<List<GeofenceResponse>>> getAllGeofences(
            @RequestParam(required = false) Long companyId) {
        log.info("GET /api/geofences - companyId: {}", companyId);

        if (companyId == null) {
            return ResponseEntity.ok(ApiResponse.success(
                "Company ID is required", List.of()));
        }

        List<GeofenceResponse> geofences = geofenceService.getGeofencesByCompany(companyId)
            .stream()
            .map(GeofenceResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
            "Geofences retrieved successfully", geofences));
    }

    /**
     * Get geofence by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get geofence by ID")
    public ResponseEntity<ApiResponse<GeofenceResponse>> getGeofenceById(@PathVariable Long id) {
        log.info("GET /api/geofences/{}", id);

        Geofence geofence = geofenceService.getGeofenceById(id);
        return ResponseEntity.ok(ApiResponse.success(
            "Geofence retrieved successfully", GeofenceResponse.from(geofence)));
    }

    /**
     * Create a new geofence
     */
    @PostMapping
    @Operation(summary = "Create a new geofence")
    public ResponseEntity<ApiResponse<GeofenceResponse>> createGeofence(
            @Valid @RequestBody GeofenceRequest request) {
        log.info("POST /api/geofences - Creating geofence: {}", request.getName());

        Geofence geofence = geofenceService.createGeofence(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Geofence created successfully", GeofenceResponse.from(geofence)));
    }

    /**
     * Update a geofence
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing geofence")
    public ResponseEntity<ApiResponse<GeofenceResponse>> updateGeofence(
            @PathVariable Long id,
            @Valid @RequestBody GeofenceRequest request) {
        log.info("PUT /api/geofences/{}", id);

        Geofence geofence = geofenceService.updateGeofence(id, request);
        return ResponseEntity.ok(ApiResponse.success(
            "Geofence updated successfully", GeofenceResponse.from(geofence)));
    }

    /**
     * Delete a geofence
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a geofence")
    public ResponseEntity<ApiResponse<Void>> deleteGeofence(@PathVariable Long id) {
        log.info("DELETE /api/geofences/{}", id);

        geofenceService.deleteGeofence(id);
        return ResponseEntity.ok(ApiResponse.success("Geofence deleted successfully", null));
    }
}
