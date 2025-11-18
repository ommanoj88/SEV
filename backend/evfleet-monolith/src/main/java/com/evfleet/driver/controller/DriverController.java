package com.evfleet.driver.controller;

import com.evfleet.common.dto.ApiResponse;
import com.evfleet.driver.dto.DriverRequest;
import com.evfleet.driver.dto.DriverResponse;
import com.evfleet.driver.service.DriverService;
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
 * Driver Controller
 * Handles all driver-related REST endpoints
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Drivers", description = "Driver Management API")
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    @Operation(summary = "Create driver")
    public ResponseEntity<ApiResponse<DriverResponse>> createDriver(
            @RequestParam Long companyId,
            @Valid @RequestBody DriverRequest request) {
        log.info("POST /api/v1/drivers - companyId: {}, request: {}", companyId, request);
        DriverResponse driver = driverService.createDriver(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver created successfully", driver));
    }

    @GetMapping
    @Operation(summary = "Get all drivers")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getAllDrivers(
            @RequestParam Long companyId) {
        log.info("GET /api/v1/drivers - companyId: {}", companyId);
        List<DriverResponse> drivers = driverService.getAllDrivers(companyId);
        return ResponseEntity.ok(ApiResponse.success("Drivers retrieved successfully", drivers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get driver by ID")
    public ResponseEntity<ApiResponse<DriverResponse>> getDriverById(@PathVariable Long id) {
        log.info("GET /api/v1/drivers/{}", id);
        DriverResponse driver = driverService.getDriverById(id);
        return ResponseEntity.ok(ApiResponse.success("Driver retrieved successfully", driver));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active drivers")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getActiveDrivers(
            @RequestParam Long companyId) {
        log.info("GET /api/v1/drivers/active - companyId: {}", companyId);
        List<DriverResponse> drivers = driverService.getActiveDrivers(companyId);
        return ResponseEntity.ok(ApiResponse.success("Active drivers retrieved successfully", drivers));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available drivers (not assigned to any vehicle)")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getAvailableDrivers(
            @RequestParam Long companyId) {
        log.info("GET /api/v1/drivers/available - companyId: {}", companyId);
        List<DriverResponse> drivers = driverService.getAvailableDrivers(companyId);
        return ResponseEntity.ok(ApiResponse.success("Available drivers retrieved successfully", drivers));
    }

    @GetMapping("/expiring-licenses")
    @Operation(summary = "Get drivers with expiring licenses")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getDriversWithExpiringLicenses(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "30") int daysAhead) {
        log.info("GET /api/v1/drivers/expiring-licenses - companyId: {}, daysAhead: {}", companyId, daysAhead);
        List<DriverResponse> drivers = driverService.getDriversWithExpiringLicenses(companyId, daysAhead);
        return ResponseEntity.ok(ApiResponse.success("Drivers with expiring licenses retrieved successfully", drivers));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update driver")
    public ResponseEntity<ApiResponse<DriverResponse>> updateDriver(
            @PathVariable Long id,
            @Valid @RequestBody DriverRequest request) {
        log.info("PUT /api/v1/drivers/{} - request: {}", id, request);
        DriverResponse driver = driverService.updateDriver(id, request);
        return ResponseEntity.ok(ApiResponse.success("Driver updated successfully", driver));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign vehicle to driver")
    public ResponseEntity<ApiResponse<DriverResponse>> assignVehicle(
            @PathVariable Long id,
            @RequestParam Long vehicleId) {
        log.info("POST /api/v1/drivers/{}/assign - vehicleId: {}", id, vehicleId);
        DriverResponse driver = driverService.assignVehicle(id, vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Vehicle assigned to driver successfully", driver));
    }

    @PostMapping("/{id}/unassign")
    @Operation(summary = "Unassign vehicle from driver")
    public ResponseEntity<ApiResponse<DriverResponse>> unassignVehicle(@PathVariable Long id) {
        log.info("POST /api/v1/drivers/{}/unassign", id);
        DriverResponse driver = driverService.unassignVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle unassigned from driver successfully", driver));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete driver")
    public ResponseEntity<ApiResponse<Void>> deleteDriver(@PathVariable Long id) {
        log.info("DELETE /api/v1/drivers/{}", id);
        driverService.deleteDriver(id);
        return ResponseEntity.ok(ApiResponse.success("Driver deleted successfully", null));
    }
}
