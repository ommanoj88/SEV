package com.evfleet.driver.controller;

import com.evfleet.driver.dto.*;
import com.evfleet.driver.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver Management", description = "APIs for managing drivers")
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    @Operation(summary = "Get all drivers", description = "Retrieve a list of all drivers")
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        List<DriverResponse> drivers = driverService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get driver by ID", description = "Retrieve a specific driver by their ID")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable String id) {
        DriverResponse driver = driverService.getDriverById(id);
        return ResponseEntity.ok(driver);
    }

    @PostMapping
    @Operation(summary = "Create a new driver", description = "Create a new driver in the system")
    public ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody DriverRequest request) {
        DriverResponse driver = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(driver);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update driver", description = "Update an existing driver's information")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable String id,
            @Valid @RequestBody DriverRequest request) {
        DriverResponse driver = driverService.updateDriver(id, request);
        return ResponseEntity.ok(driver);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete driver", description = "Delete a driver from the system")
    public ResponseEntity<Void> deleteDriver(@PathVariable String id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get drivers by company", description = "Retrieve all drivers for a specific company")
    public ResponseEntity<List<DriverResponse>> getDriversByCompany(@PathVariable String companyId) {
        List<DriverResponse> drivers = driverService.getDriversByCompany(companyId);
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/{driverId}/behavior")
    @Operation(summary = "Get driver behavior", description = "Retrieve behavior records for a specific driver")
    public ResponseEntity<List<DriverBehaviorResponse>> getDriverBehavior(@PathVariable String driverId) {
        List<DriverBehaviorResponse> behaviors = driverService.getDriverBehavior(driverId);
        return ResponseEntity.ok(behaviors);
    }

    @PostMapping("/{driverId}/assign")
    @Operation(summary = "Assign driver to vehicle", description = "Assign a driver to a vehicle for a specific shift")
    public ResponseEntity<DriverAssignmentResponse> assignDriverToVehicle(
            @PathVariable String driverId,
            @Valid @RequestBody DriverAssignmentRequest request) {
        DriverAssignmentResponse assignment = driverService.assignDriverToVehicle(driverId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    @GetMapping("/{driverId}/assignments")
    @Operation(summary = "Get driver assignments", description = "Retrieve all assignments for a specific driver")
    public ResponseEntity<List<DriverAssignmentResponse>> getDriverAssignments(@PathVariable String driverId) {
        List<DriverAssignmentResponse> assignments = driverService.getDriverAssignments(driverId);
        return ResponseEntity.ok(assignments);
    }
}
