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
import java.util.stream.Collectors;

/**
 * Vehicle Controller
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fleet - Vehicles", description = "Vehicle Management API")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    @Operation(summary = "Get all vehicles")
    public ResponseEntity<com.evfleet.common.dto.ApiResponse<List<VehicleResponse>>> getAllVehicles(
            @RequestParam(required = false) Long companyId) {
        log.info("GET /api/v1/vehicles - companyId: {}", companyId);
        List<VehicleResponse> vehicles;

        if (companyId != null) {
            vehicles = vehicleService.getVehiclesWithDriverNames(companyId);
        } else {
            // Return empty list for now - in production, would need pagination
            vehicles = List.of();
        }

        return ResponseEntity.ok(com.evfleet.common.dto.ApiResponse.success(
            "Vehicles retrieved successfully", vehicles));
    }

    @PostMapping
    @Operation(summary = "Register new vehicle")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {
        log.info("POST /api/v1/vehicles - Creating vehicle: {}", request.getVehicleNumber());

        Vehicle vehicle = Vehicle.builder()
            .companyId(request.getCompanyId())
            .vehicleNumber(request.getVehicleNumber())
            .type(request.getType())
            .fuelType(request.getFuelType())
            .make(request.getMake())
            .model(request.getModel())
            .year(request.getYear())
            .batteryCapacity(request.getBatteryCapacity())
            .currentBatterySoc(request.getCurrentBatterySoc())
            .defaultChargerType(request.getDefaultChargerType())
            .fuelTankCapacity(request.getFuelTankCapacity())
            .fuelLevel(request.getFuelLevel())
            .engineType(request.getEngineType())
            .status(Vehicle.VehicleStatus.ACTIVE)
            .vin(request.getVin())
            .licensePlate(request.getLicensePlate())
            .color(request.getColor())
            .build();

        Vehicle created = vehicleService.createVehicle(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(VehicleResponse.from(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<VehicleResponse> getVehicle(@PathVariable Long id) {
        log.info("GET /api/v1/vehicles/{}", id);
        Vehicle vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(VehicleResponse.from(vehicle));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get vehicles by company")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByCompany(@PathVariable Long companyId) {
        log.info("GET /api/v1/vehicles/company/{}", companyId);
        List<VehicleResponse> vehicles = vehicleService.getVehiclesWithDriverNames(companyId);
        return ResponseEntity.ok(vehicles);
    }

    @PutMapping("/{id}/location")
    @Operation(summary = "Update vehicle location")
    public ResponseEntity<VehicleResponse> updateLocation(
            @PathVariable Long id,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        log.info("PUT /api/v1/vehicles/{}/location - lat: {}, lon: {}", id, latitude, longitude);
        Vehicle updated = vehicleService.updateVehicleLocation(id, latitude, longitude);
        return ResponseEntity.ok(VehicleResponse.from(updated));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Fleet Service is running");
    }
}
