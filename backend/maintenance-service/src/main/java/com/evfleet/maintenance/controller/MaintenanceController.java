package com.evfleet.maintenance.controller;

import com.evfleet.maintenance.dto.MaintenanceRecordResponse;
import com.evfleet.maintenance.entity.BatteryHealth;
import com.evfleet.maintenance.entity.MaintenanceSchedule;
import com.evfleet.maintenance.entity.ServiceHistory;
import com.evfleet.maintenance.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "Maintenance management endpoints")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    // ========== MAINTENANCE RECORDS (Combined view) ==========
    
    @GetMapping("/records")
    @Operation(summary = "Get all maintenance records", description = "Retrieves all maintenance records combining schedules and service history")
    public ResponseEntity<List<MaintenanceRecordResponse>> getAllMaintenanceRecords() {
        List<MaintenanceRecordResponse> records = maintenanceService.getAllMaintenanceRecords();
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/records/{id}")
    @Operation(summary = "Get maintenance record by ID", description = "Retrieves a specific maintenance record by ID")
    public ResponseEntity<MaintenanceRecordResponse> getMaintenanceRecordById(@PathVariable String id) {
        Optional<MaintenanceRecordResponse> record = maintenanceService.getMaintenanceRecordById(id);
        return record.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/records/vehicle/{vehicleId}")
    @Operation(summary = "Get maintenance records by vehicle", description = "Retrieves maintenance records for a specific vehicle")
    public ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceRecordsByVehicle(
            @PathVariable String vehicleId) {
        List<MaintenanceRecordResponse> records = maintenanceService.getMaintenanceRecordsByVehicle(vehicleId);
        return ResponseEntity.ok(records);
    }
    
    @PostMapping("/records")
    @Operation(summary = "Create service history record", description = "Creates a new service history record")
    public ResponseEntity<ServiceHistory> createServiceHistory(@Valid @RequestBody ServiceHistory serviceHistory) {
        ServiceHistory created = maintenanceService.createServiceHistory(serviceHistory);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/records/{id}")
    @Operation(summary = "Update service history record", description = "Updates an existing service history record")
    public ResponseEntity<ServiceHistory> updateServiceHistory(
            @PathVariable String id,
            @Valid @RequestBody ServiceHistory serviceHistory) {
        ServiceHistory updated = maintenanceService.updateServiceHistory(id, serviceHistory);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/records/{id}")
    @Operation(summary = "Delete service history record", description = "Deletes a service history record")
    public ResponseEntity<Void> deleteServiceHistory(@PathVariable String id) {
        maintenanceService.deleteServiceHistory(id);
        return ResponseEntity.noContent().build();
    }
    
    // ========== MAINTENANCE SCHEDULES ==========

    @GetMapping("/schedules")
    @Operation(summary = "Get all maintenance schedules", description = "Retrieves all maintenance schedules")
    public ResponseEntity<List<MaintenanceSchedule>> getAllMaintenanceSchedules() {
        List<MaintenanceSchedule> schedules = maintenanceService.getAllMaintenanceSchedules();
        return ResponseEntity.ok(schedules);
    }
    
    @GetMapping("/schedules/{id}")
    @Operation(summary = "Get maintenance schedule by ID", description = "Retrieves a specific maintenance schedule by ID")
    public ResponseEntity<MaintenanceSchedule> getMaintenanceScheduleById(@PathVariable String id) {
        Optional<MaintenanceSchedule> schedule = maintenanceService.getMaintenanceScheduleById(id);
        return schedule.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/schedules")
    @Operation(summary = "Create maintenance schedule", description = "Creates a new maintenance schedule for a vehicle")
    public ResponseEntity<MaintenanceSchedule> createMaintenanceSchedule(
            @Valid @RequestBody MaintenanceSchedule schedule) {
        MaintenanceSchedule createdSchedule = maintenanceService.createMaintenanceSchedule(schedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
    }
    
    @GetMapping("/schedules/vehicle/{vehicleId}")
    @Operation(summary = "Get maintenance schedules by vehicle", description = "Retrieves maintenance schedules for a specific vehicle")
    public ResponseEntity<List<MaintenanceSchedule>> getMaintenanceSchedulesByVehicle(@PathVariable String vehicleId) {
        List<MaintenanceSchedule> schedules = maintenanceService.getMaintenanceSchedulesByVehicle(vehicleId);
        return ResponseEntity.ok(schedules);
    }
    
    @PutMapping("/schedules/{id}")
    @Operation(summary = "Update maintenance schedule", description = "Updates an existing maintenance schedule")
    public ResponseEntity<MaintenanceSchedule> updateMaintenanceSchedule(
            @PathVariable String id,
            @Valid @RequestBody MaintenanceSchedule schedule) {
        MaintenanceSchedule updated = maintenanceService.updateMaintenanceSchedule(id, schedule);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/schedules/{id}")
    @Operation(summary = "Delete maintenance schedule", description = "Deletes a maintenance schedule")
    public ResponseEntity<Void> deleteMaintenanceSchedule(@PathVariable String id) {
        maintenanceService.deleteMaintenanceSchedule(id);
        return ResponseEntity.noContent().build();
    }
    
    // ========== BATTERY HEALTH ==========

    @GetMapping("/battery-health/{vehicleId}")
    @Operation(summary = "Get battery health by vehicle", description = "Retrieves battery health records for a specific vehicle")
    public ResponseEntity<List<BatteryHealth>> getBatteryHealthByVehicle(
            @PathVariable String vehicleId) {
        List<BatteryHealth> batteryHealthRecords = maintenanceService.getBatteryHealthByVehicle(vehicleId);
        return ResponseEntity.ok(batteryHealthRecords);
    }
    
    @PostMapping("/battery-health")
    @Operation(summary = "Create battery health record", description = "Creates a new battery health record")
    public ResponseEntity<BatteryHealth> createBatteryHealth(@Valid @RequestBody BatteryHealth batteryHealth) {
        BatteryHealth created = maintenanceService.createBatteryHealth(batteryHealth);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    // ========== SERVICE HISTORY ==========
    
    @GetMapping("/service-history/{vehicleId}")
    @Operation(summary = "Get service history by vehicle", description = "Retrieves service history for a specific vehicle")
    public ResponseEntity<List<ServiceHistory>> getServiceHistory(@PathVariable String vehicleId) {
        List<ServiceHistory> history = maintenanceService.getServiceHistoryByVehicle(vehicleId);
        return ResponseEntity.ok(history);
    }
    
    // ========== SERVICE REMINDERS ==========
    
    @GetMapping("/reminders")
    @Operation(summary = "Get service reminders", description = "Retrieves upcoming service reminders")
    public ResponseEntity<List<MaintenanceSchedule>> getServiceReminders() {
        List<MaintenanceSchedule> reminders = maintenanceService.getServiceReminders();
        return ResponseEntity.ok(reminders);
    }
}
