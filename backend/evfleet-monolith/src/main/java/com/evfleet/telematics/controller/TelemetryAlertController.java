package com.evfleet.telematics.controller;

import com.evfleet.common.dto.ApiResponse;
import com.evfleet.telematics.model.TelemetryAlert;
import com.evfleet.telematics.service.TelemetryAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Telemetry Alert Controller
 * Endpoints for managing telemetry-based alerts
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/telematics/alerts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Telemetry Alerts", description = "Telemetry Alert Management API")
public class TelemetryAlertController {

    private final TelemetryAlertService alertService;

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get all active alerts for a company")
    public ResponseEntity<ApiResponse<List<TelemetryAlert>>> getActiveAlerts(
            @PathVariable Long companyId) {
        log.info("GET /api/v1/telematics/alerts/company/{}", companyId);
        
        List<TelemetryAlert> alerts = alertService.getActiveAlerts(companyId);
        return ResponseEntity.ok(ApiResponse.success("Active alerts retrieved", alerts));
    }

    @GetMapping("/company/{companyId}/urgent")
    @Operation(summary = "Get critical and high priority alerts for a company")
    public ResponseEntity<ApiResponse<List<TelemetryAlert>>> getUrgentAlerts(
            @PathVariable Long companyId) {
        log.info("GET /api/v1/telematics/alerts/company/{}/urgent", companyId);
        
        List<TelemetryAlert> alerts = alertService.getUrgentAlerts(companyId);
        return ResponseEntity.ok(ApiResponse.success("Urgent alerts retrieved", alerts));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get all alerts for a specific vehicle")
    public ResponseEntity<ApiResponse<List<TelemetryAlert>>> getVehicleAlerts(
            @PathVariable Long vehicleId) {
        log.info("GET /api/v1/telematics/alerts/vehicle/{}", vehicleId);
        
        List<TelemetryAlert> alerts = alertService.getVehicleAlerts(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Vehicle alerts retrieved", alerts));
    }

    @PostMapping("/{alertId}/acknowledge")
    @Operation(summary = "Acknowledge an alert")
    public ResponseEntity<ApiResponse<TelemetryAlert>> acknowledgeAlert(
            @PathVariable Long alertId,
            @RequestParam Long userId) {
        log.info("POST /api/v1/telematics/alerts/{}/acknowledge - userId: {}", alertId, userId);
        
        TelemetryAlert alert = alertService.acknowledgeAlert(alertId, userId);
        return ResponseEntity.ok(ApiResponse.success("Alert acknowledged", alert));
    }

    @PostMapping("/{alertId}/resolve")
    @Operation(summary = "Resolve an alert")
    public ResponseEntity<ApiResponse<TelemetryAlert>> resolveAlert(
            @PathVariable Long alertId,
            @RequestParam Long userId,
            @RequestParam(required = false) String notes) {
        log.info("POST /api/v1/telematics/alerts/{}/resolve - userId: {}", alertId, userId);
        
        TelemetryAlert alert = alertService.resolveAlert(alertId, userId, notes);
        return ResponseEntity.ok(ApiResponse.success("Alert resolved", alert));
    }

    @PostMapping("/vehicle/{vehicleId}/acknowledge-all")
    @Operation(summary = "Acknowledge all alerts for a vehicle")
    public ResponseEntity<ApiResponse<Integer>> acknowledgeAllForVehicle(
            @PathVariable Long vehicleId,
            @RequestParam Long userId) {
        log.info("POST /api/v1/telematics/alerts/vehicle/{}/acknowledge-all - userId: {}", vehicleId, userId);
        
        int count = alertService.acknowledgeAllForVehicle(vehicleId, userId);
        return ResponseEntity.ok(ApiResponse.success(count + " alerts acknowledged", count));
    }

    @GetMapping("/company/{companyId}/stats")
    @Operation(summary = "Get alert statistics for a company")
    public ResponseEntity<ApiResponse<TelemetryAlertService.AlertStatistics>> getAlertStatistics(
            @PathVariable Long companyId) {
        log.info("GET /api/v1/telematics/alerts/company/{}/stats", companyId);
        
        TelemetryAlertService.AlertStatistics stats = alertService.getAlertStatistics(companyId);
        return ResponseEntity.ok(ApiResponse.success("Alert statistics retrieved", stats));
    }

    @PostMapping("/check-connections")
    @Operation(summary = "Manually trigger connection lost check")
    public ResponseEntity<ApiResponse<List<TelemetryAlert>>> checkConnectionLost() {
        log.info("POST /api/v1/telematics/alerts/check-connections");
        
        List<TelemetryAlert> alerts = alertService.checkConnectionLostAlerts();
        String message = alerts.isEmpty() ? "No connection issues found" : alerts.size() + " connection alerts generated";
        return ResponseEntity.ok(ApiResponse.success(message, alerts));
    }
}
