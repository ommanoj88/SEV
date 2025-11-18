package com.evfleet.maintenance.controller;

import com.evfleet.common.dto.ApiResponse;
import com.evfleet.maintenance.dto.MaintenanceRecordRequest;
import com.evfleet.maintenance.dto.MaintenanceRecordResponse;
import com.evfleet.maintenance.service.MaintenanceService;
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
 * Maintenance Controller
 * Handles all maintenance-related REST endpoints
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Maintenance", description = "Vehicle Maintenance Management API")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping("/records")
    @Operation(summary = "Create maintenance record")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> createMaintenanceRecord(
            @RequestParam Long companyId,
            @Valid @RequestBody MaintenanceRecordRequest request) {
        log.info("POST /api/v1/maintenance/records - companyId: {}, request: {}", companyId, request);
        MaintenanceRecordResponse record = maintenanceService.createMaintenanceRecord(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Maintenance record created successfully", record));
    }

    @GetMapping("/records")
    @Operation(summary = "Get all maintenance records")
    public ResponseEntity<ApiResponse<List<MaintenanceRecordResponse>>> getAllMaintenanceRecords(
            @RequestParam Long companyId) {
        log.info("GET /api/v1/maintenance/records - companyId: {}", companyId);
        List<MaintenanceRecordResponse> records = maintenanceService.getAllMaintenanceRecords(companyId);
        return ResponseEntity.ok(ApiResponse.success("Maintenance records retrieved successfully", records));
    }

    @GetMapping("/records/{id}")
    @Operation(summary = "Get maintenance record by ID")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> getMaintenanceRecordById(
            @PathVariable Long id) {
        log.info("GET /api/v1/maintenance/records/{}", id);
        MaintenanceRecordResponse record = maintenanceService.getMaintenanceRecordById(id);
        return ResponseEntity.ok(ApiResponse.success("Maintenance record retrieved successfully", record));
    }

    @GetMapping("/records/vehicle/{vehicleId}")
    @Operation(summary = "Get maintenance records by vehicle")
    public ResponseEntity<ApiResponse<List<MaintenanceRecordResponse>>> getMaintenanceRecordsByVehicle(
            @PathVariable Long vehicleId) {
        log.info("GET /api/v1/maintenance/records/vehicle/{}", vehicleId);
        List<MaintenanceRecordResponse> records = maintenanceService.getMaintenanceRecordsByVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Maintenance records retrieved successfully", records));
    }

    @GetMapping("/records/upcoming")
    @Operation(summary = "Get upcoming maintenance")
    public ResponseEntity<ApiResponse<List<MaintenanceRecordResponse>>> getUpcomingMaintenance(
            @RequestParam Long companyId) {
        log.info("GET /api/v1/maintenance/records/upcoming - companyId: {}", companyId);
        List<MaintenanceRecordResponse> records = maintenanceService.getUpcomingMaintenance(companyId);
        return ResponseEntity.ok(ApiResponse.success("Upcoming maintenance retrieved successfully", records));
    }

    @PutMapping("/records/{id}")
    @Operation(summary = "Update maintenance record")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> updateMaintenanceRecord(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceRecordRequest request) {
        log.info("PUT /api/v1/maintenance/records/{} - request: {}", id, request);
        MaintenanceRecordResponse record = maintenanceService.updateMaintenanceRecord(id, request);
        return ResponseEntity.ok(ApiResponse.success("Maintenance record updated successfully", record));
    }

    @PostMapping("/records/{id}/complete")
    @Operation(summary = "Complete maintenance")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> completeMaintenance(
            @PathVariable Long id) {
        log.info("POST /api/v1/maintenance/records/{}/complete", id);
        MaintenanceRecordResponse record = maintenanceService.completeMaintenance(id);
        return ResponseEntity.ok(ApiResponse.success("Maintenance completed successfully", record));
    }

    @DeleteMapping("/records/{id}")
    @Operation(summary = "Delete maintenance record")
    public ResponseEntity<ApiResponse<Void>> deleteMaintenanceRecord(@PathVariable Long id) {
        log.info("DELETE /api/v1/maintenance/records/{}", id);
        maintenanceService.deleteMaintenanceRecord(id);
        return ResponseEntity.ok(ApiResponse.success("Maintenance record deleted successfully", null));
    }
}
