package com.evfleet.fleet.controller;

import com.evfleet.fleet.dto.VehicleRequest;
import com.evfleet.fleet.dto.VehicleResponse;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.service.VehicleService;
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
 * REST Controller for Vehicle operations
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicle Management", description = "APIs for managing fleet vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @Operation(summary = "Create a new vehicle", description = "Add a new vehicle to the fleet")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {
        log.info("REST request to create vehicle: {}", request.getVehicleNumber());
        VehicleResponse response = vehicleService.createVehicle(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a vehicle", description = "Update an existing vehicle's information")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request) {
        log.info("REST request to update vehicle ID: {}", id);
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vehicle", description = "Remove a vehicle from the fleet")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        log.info("REST request to delete vehicle ID: {}", id);
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Retrieve vehicle details by ID")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
        log.info("REST request to get vehicle ID: {}", id);
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all vehicles", description = "Retrieve all vehicles in the system")
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        log.info("REST request to get all vehicles");
        List<VehicleResponse> response = vehicleService.getAllVehicles();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get vehicles by company", description = "Retrieve all vehicles for a specific company")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByCompany(@PathVariable Long companyId) {
        log.info("REST request to get vehicles for company ID: {}", companyId);
        List<VehicleResponse> response = vehicleService.getVehiclesByCompany(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}/status/{status}")
    @Operation(summary = "Get vehicles by company and status", description = "Retrieve vehicles for a company filtered by status")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByCompanyAndStatus(
            @PathVariable Long companyId,
            @PathVariable Vehicle.VehicleStatus status) {
        log.info("REST request to get vehicles for company ID: {} with status: {}", companyId, status);
        List<VehicleResponse> response = vehicleService.getVehiclesByCompanyAndStatus(companyId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{vehicleNumber}")
    @Operation(summary = "Get vehicle by number", description = "Retrieve vehicle details by vehicle number")
    public ResponseEntity<VehicleResponse> getVehicleByNumber(@PathVariable String vehicleNumber) {
        log.info("REST request to get vehicle by number: {}", vehicleNumber);
        VehicleResponse response = vehicleService.getVehicleByNumber(vehicleNumber);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/location")
    @Operation(summary = "Update vehicle location", description = "Update the current location of a vehicle")
    public ResponseEntity<Void> updateVehicleLocation(
            @PathVariable Long id,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        log.info("REST request to update location for vehicle ID: {}", id);
        vehicleService.updateVehicleLocation(id, latitude, longitude);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/battery")
    @Operation(summary = "Update battery SOC", description = "Update the current battery state of charge")
    public ResponseEntity<Void> updateBatterySoc(
            @PathVariable Long id,
            @RequestParam Double soc) {
        log.info("REST request to update battery SOC for vehicle ID: {}", id);
        vehicleService.updateBatterySoc(id, soc);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update vehicle status", description = "Update the operational status of a vehicle")
    public ResponseEntity<Void> updateVehicleStatus(
            @PathVariable Long id,
            @RequestParam Vehicle.VehicleStatus status) {
        log.info("REST request to update status for vehicle ID: {}", id);
        vehicleService.updateVehicleStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/driver")
    @Operation(summary = "Assign driver", description = "Assign a driver to a vehicle")
    public ResponseEntity<Void> assignDriver(
            @PathVariable Long id,
            @RequestParam Long driverId) {
        log.info("REST request to assign driver {} to vehicle ID: {}", driverId, id);
        vehicleService.assignDriver(id, driverId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/driver")
    @Operation(summary = "Remove driver", description = "Remove the assigned driver from a vehicle")
    public ResponseEntity<Void> removeDriver(@PathVariable Long id) {
        log.info("REST request to remove driver from vehicle ID: {}", id);
        vehicleService.removeDriver(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/company/{companyId}/low-battery")
    @Operation(summary = "Get vehicles with low battery", description = "Retrieve vehicles with battery below threshold")
    public ResponseEntity<List<VehicleResponse>> getVehiclesWithLowBattery(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "20") Double threshold) {
        log.info("REST request to get vehicles with battery below {}% for company ID: {}", threshold, companyId);
        List<VehicleResponse> response = vehicleService.getVehiclesWithLowBattery(companyId, threshold);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}/active")
    @Operation(summary = "Get active vehicles", description = "Retrieve all active vehicles for a company")
    public ResponseEntity<List<VehicleResponse>> getActiveVehicles(@PathVariable Long companyId) {
        log.info("REST request to get active vehicles for company ID: {}", companyId);
        List<VehicleResponse> response = vehicleService.getActiveVehicles(companyId);
        return ResponseEntity.ok(response);
    }
}
