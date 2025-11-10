package com.evfleet.fleet.controller;

import com.evfleet.fleet.dto.AvailableFeaturesDTO;
import com.evfleet.fleet.dto.VehicleRequest;
import com.evfleet.fleet.dto.VehicleResponse;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.service.FeatureAvailabilityService;
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
    private final FeatureAvailabilityService featureAvailabilityService;

    @PostMapping
    @Operation(summary = "Create a new vehicle", 
               description = "Add a new vehicle to the fleet. Validates fuel-type-specific requirements: " +
                           "EV/HYBRID vehicles require batteryCapacity, ICE/HYBRID vehicles require fuelTankCapacity. " +
                           "Response includes available features based on fuel type.")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {
        log.info("REST request to create vehicle: {}", request.getVehicleNumber());
        VehicleResponse response = vehicleService.createVehicle(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a vehicle", 
               description = "Update an existing vehicle's information. Validates fuel-type-specific requirements: " +
                           "EV/HYBRID vehicles require batteryCapacity, ICE/HYBRID vehicles require fuelTankCapacity. " +
                           "Response includes available features based on fuel type.")
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

    @GetMapping("/status/{status}")
    @Operation(summary = "Get vehicles by status", description = "Retrieve vehicles filtered by status")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByStatusOnly(@PathVariable String status) {
        log.info("REST request to get vehicles with status: {}", status);
        // Note: This should be implemented in service layer to get all vehicles by status
        // For now, returning empty list as placeholder
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/low-battery")
    @Operation(summary = "Get low battery vehicles", description = "Retrieve vehicles with low battery")
    public ResponseEntity<List<VehicleResponse>> getLowBatteryVehiclesAll(
            @RequestParam(defaultValue = "20") Double threshold) {
        log.info("REST request to get vehicles with battery below {}%", threshold);
        // Note: This should be implemented in service layer to get all low battery vehicles
        // For now, returning empty list as placeholder
        return ResponseEntity.ok(List.of());
    }

    @PatchMapping("/{id}/location")
    @Operation(summary = "Update vehicle location", description = "Update the current location of a vehicle")
    public ResponseEntity<Void> updateVehicleLocation(
            @PathVariable Long id,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestBody(required = false) java.util.Map<String, Double> location) {
        log.info("REST request to update location for vehicle ID: {}", id);
        
        // Support both query params and request body
        Double lat = latitude != null ? latitude : (location != null ? location.get("latitude") : null);
        Double lon = longitude != null ? longitude : (location != null ? location.get("longitude") : null);
        
        if (lat != null && lon != null) {
            vehicleService.updateVehicleLocation(id, lat, lon);
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/battery")
    @Operation(summary = "Update battery SOC", description = "Update the current battery state of charge")
    public ResponseEntity<Void> updateBatterySoc(
            @PathVariable Long id,
            @RequestParam(required = false) Double soc,
            @RequestBody(required = false) java.util.Map<String, Double> battery) {
        log.info("REST request to update battery SOC for vehicle ID: {}", id);
        
        // Support both query param and request body (currentBatterySoc or soc)
        Double batteryLevel = soc != null ? soc : (battery != null ? 
            (battery.get("currentBatterySoc") != null ? battery.get("currentBatterySoc") : battery.get("soc")) 
            : null);
        
        if (batteryLevel != null) {
            vehicleService.updateBatterySoc(id, batteryLevel);
        }
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

    // ===== PR 4: Multi-fuel Query Endpoints =====

    @GetMapping("/fuel-type/{fuelType}")
    @Operation(summary = "Get vehicles by fuel type", description = "Retrieve all vehicles with a specific fuel type (ICE, EV, or HYBRID)")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByFuelType(
            @PathVariable com.evfleet.fleet.model.FuelType fuelType) {
        log.info("REST request to get vehicles with fuel type: {}", fuelType);
        List<VehicleResponse> response = vehicleService.getVehiclesByFuelType(fuelType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}/fuel-type/{fuelType}")
    @Operation(summary = "Get vehicles by company and fuel type", 
               description = "Retrieve vehicles for a specific company filtered by fuel type")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByCompanyAndFuelType(
            @PathVariable Long companyId,
            @PathVariable com.evfleet.fleet.model.FuelType fuelType) {
        log.info("REST request to get vehicles for company ID: {} with fuel type: {}", companyId, fuelType);
        List<VehicleResponse> response = vehicleService.getVehiclesByCompanyAndFuelType(companyId, fuelType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}/fleet-composition")
    @Operation(summary = "Get fleet composition", 
               description = "Get the distribution of vehicle fuel types in the fleet (counts and percentages)")
    public ResponseEntity<java.util.Map<String, Object>> getFleetComposition(@PathVariable Long companyId) {
        log.info("REST request to get fleet composition for company ID: {}", companyId);
        java.util.Map<String, Object> composition = vehicleService.getFleetComposition(companyId);
        return ResponseEntity.ok(composition);
    }

    @GetMapping("/company/{companyId}/low-battery-vehicles")
    @Operation(summary = "Get low battery vehicles", 
               description = "Retrieve EV/HYBRID vehicles with battery below threshold")
    public ResponseEntity<List<VehicleResponse>> getLowBatteryVehicles(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "20") Double threshold) {
        log.info("REST request to get EV/HYBRID vehicles with battery below {}% for company ID: {}", threshold, companyId);
        List<VehicleResponse> response = vehicleService.getLowBatteryVehicles(companyId, threshold);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}/low-fuel-vehicles")
    @Operation(summary = "Get low fuel vehicles", 
               description = "Retrieve ICE/HYBRID vehicles with fuel level below threshold percentage")
    public ResponseEntity<List<VehicleResponse>> getLowFuelVehicles(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "20") Double threshold) {
        log.info("REST request to get ICE/HYBRID vehicles with fuel below {}% for company ID: {}", threshold, companyId);
        List<VehicleResponse> response = vehicleService.getLowFuelVehicles(companyId, threshold);
        return ResponseEntity.ok(response);
    }

    // ===== PR 8: Feature Availability Endpoint =====

    @GetMapping("/{id}/available-features")
    @Operation(summary = "Get available features for a vehicle",
               description = "Returns a list of features available for a vehicle based on its fuel type. " +
                           "EV vehicles support battery tracking, charging management, and energy analytics. " +
                           "ICE vehicles support fuel consumption tracking, fuel station discovery, and engine diagnostics. " +
                           "HYBRID vehicles support all features from both EV and ICE.")
    public ResponseEntity<AvailableFeaturesDTO> getAvailableFeatures(@PathVariable Long id) {
        log.info("REST request to get available features for vehicle ID: {}", id);
        VehicleResponse vehicleResponse = vehicleService.getVehicleById(id);
        
        // Create a minimal Vehicle object with fuel type for feature lookup
        Vehicle vehicle = new Vehicle();
        vehicle.setFuelType(vehicleResponse.getFuelType());
        
        AvailableFeaturesDTO features = featureAvailabilityService.buildAvailableFeatures(vehicle);
        return ResponseEntity.ok(features);
    }
}
