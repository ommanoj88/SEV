package com.evfleet.fleet.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.fleet.dto.BatteryHealthRequest;
import com.evfleet.fleet.dto.BatteryHealthResponse;
import com.evfleet.fleet.model.BatteryHealth;
import com.evfleet.fleet.repository.BatteryHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Battery Health Service
 * Handles all battery health monitoring business logic
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BatteryHealthService {

    private final BatteryHealthRepository batteryHealthRepository;

    /**
     * Record new battery health data
     */
    public BatteryHealthResponse recordBatteryHealth(BatteryHealthRequest request) {
        log.info("Recording battery health for vehicle: {}", request.getVehicleId());

        BatteryHealth health = BatteryHealth.builder()
                .vehicleId(request.getVehicleId())
                .soh(request.getSoh())
                .cycleCount(request.getCycleCount())
                .temperature(request.getTemperature())
                .internalResistance(request.getInternalResistance())
                .voltageDeviation(request.getVoltageDeviation())
                .currentSoc(request.getCurrentSoc())
                .recordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        BatteryHealth saved = batteryHealthRepository.save(health);
        log.info("Battery health recorded successfully: {}", saved.getId());
        return BatteryHealthResponse.fromEntity(saved);
    }

    /**
     * Get battery health history for a vehicle
     */
    @Transactional(readOnly = true)
    public List<BatteryHealthResponse> getBatteryHealthHistory(Long vehicleId) {
        log.info("Fetching battery health history for vehicle: {}", vehicleId);
        List<BatteryHealth> history = batteryHealthRepository.findByVehicleIdOrderByRecordedAtDesc(vehicleId);
        return history.stream()
                .map(BatteryHealthResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get battery health history for a vehicle within a date range
     */
    @Transactional(readOnly = true)
    public List<BatteryHealthResponse> getBatteryHealthHistoryByDateRange(
            Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching battery health history for vehicle: {} from {} to {}", vehicleId, startTime, endTime);
        List<BatteryHealth> history = batteryHealthRepository.findByVehicleIdAndRecordedAtBetweenOrderByRecordedAtDesc(
                vehicleId, startTime, endTime);
        return history.stream()
                .map(BatteryHealthResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get latest battery health for a vehicle
     */
    @Transactional(readOnly = true)
    public BatteryHealthResponse getLatestBatteryHealth(Long vehicleId) {
        log.info("Fetching latest battery health for vehicle: {}", vehicleId);
        BatteryHealth health = batteryHealthRepository.findFirstByVehicleIdOrderByRecordedAtDesc(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("BatteryHealth", "vehicleId", vehicleId));
        return BatteryHealthResponse.fromEntity(health);
    }

    /**
     * Find vehicles with low SOH (State of Health)
     */
    @Transactional(readOnly = true)
    public List<Long> findVehiclesWithLowSoh(Double threshold) {
        log.info("Finding vehicles with SOH below: {}%", threshold);
        return batteryHealthRepository.findVehicleIdsWithSohBelow(threshold);
    }
}
