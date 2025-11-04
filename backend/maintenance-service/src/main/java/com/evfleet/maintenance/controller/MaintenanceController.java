package com.evfleet.maintenance.controller;

import com.evfleet.maintenance.dto.MaintenanceRecordResponse;
import com.evfleet.maintenance.entity.BatteryHealth;
import com.evfleet.maintenance.entity.MaintenanceSchedule;
import com.evfleet.maintenance.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance/records")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "Maintenance management endpoints")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @GetMapping
    @Operation(summary = "Get all maintenance records", description = "Retrieves all maintenance records combining schedules and service history")
    public ResponseEntity<List<MaintenanceRecordResponse>> getAllMaintenanceRecords() {
        List<MaintenanceRecordResponse> records = maintenanceService.getAllMaintenanceRecords();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get maintenance records by vehicle", description = "Retrieves maintenance records for a specific vehicle")
    public ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceRecordsByVehicle(
            @PathVariable String vehicleId) {
        List<MaintenanceRecordResponse> records = maintenanceService.getMaintenanceRecordsByVehicle(vehicleId);
        return ResponseEntity.ok(records);
    }

    @PostMapping("/schedules")
    @Operation(summary = "Create maintenance schedule", description = "Creates a new maintenance schedule for a vehicle")
    public ResponseEntity<MaintenanceSchedule> createMaintenanceSchedule(
            @Valid @RequestBody MaintenanceSchedule schedule) {
        MaintenanceSchedule createdSchedule = maintenanceService.createMaintenanceSchedule(schedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
    }

    @GetMapping("/battery/{vehicleId}")
    @Operation(summary = "Get battery health by vehicle", description = "Retrieves battery health records for a specific vehicle")
    public ResponseEntity<List<BatteryHealth>> getBatteryHealthByVehicle(
            @PathVariable String vehicleId) {
        List<BatteryHealth> batteryHealthRecords = maintenanceService.getBatteryHealthByVehicle(vehicleId);
        return ResponseEntity.ok(batteryHealthRecords);
    }
}
