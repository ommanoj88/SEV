package com.evfleet.fleet.controller;

import com.evfleet.fleet.dto.BatteryHealthRequest;
import com.evfleet.fleet.dto.BatteryHealthResponse;
import com.evfleet.fleet.service.BatteryHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Battery Health Controller
 * REST API endpoints for battery health monitoring
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/battery-health")
@RequiredArgsConstructor
@Slf4j
public class BatteryHealthController {

    private final BatteryHealthService batteryHealthService;

    /**
     * Record new battery health data
     */
    @PostMapping
    public ResponseEntity<BatteryHealthResponse> recordBatteryHealth(@RequestBody BatteryHealthRequest request) {
        log.info("POST /api/v1/battery-health - Recording battery health for vehicle: {}", request.getVehicleId());
        BatteryHealthResponse response = batteryHealthService.recordBatteryHealth(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get battery health history for a vehicle
     */
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<BatteryHealthResponse>> getBatteryHealthHistory(@PathVariable Long vehicleId) {
        log.info("GET /api/v1/battery-health/vehicle/{} - Fetching battery health history", vehicleId);
        List<BatteryHealthResponse> history = batteryHealthService.getBatteryHealthHistory(vehicleId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get battery health history for a vehicle within a date range
     */
    @GetMapping("/vehicle/{vehicleId}/range")
    public ResponseEntity<List<BatteryHealthResponse>> getBatteryHealthHistoryByDateRange(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("GET /api/v1/battery-health/vehicle/{}/range - Fetching history from {} to {}", 
                vehicleId, startTime, endTime);
        List<BatteryHealthResponse> history = batteryHealthService.getBatteryHealthHistoryByDateRange(
                vehicleId, startTime, endTime);
        return ResponseEntity.ok(history);
    }

    /**
     * Get latest battery health for a vehicle
     */
    @GetMapping("/vehicle/{vehicleId}/latest")
    public ResponseEntity<BatteryHealthResponse> getLatestBatteryHealth(@PathVariable Long vehicleId) {
        log.info("GET /api/v1/battery-health/vehicle/{}/latest - Fetching latest battery health", vehicleId);
        BatteryHealthResponse response = batteryHealthService.getLatestBatteryHealth(vehicleId);
        return ResponseEntity.ok(response);
    }

    /**
     * Find vehicles with low SOH
     */
    @GetMapping("/low-soh")
    public ResponseEntity<List<Long>> findVehiclesWithLowSoh(
            @RequestParam(defaultValue = "80.0") Double threshold) {
        log.info("GET /api/v1/battery-health/low-soh - Finding vehicles with SOH below {}%", threshold);
        List<Long> vehicleIds = batteryHealthService.findVehiclesWithLowSoh(threshold);
        return ResponseEntity.ok(vehicleIds);
    }
}
