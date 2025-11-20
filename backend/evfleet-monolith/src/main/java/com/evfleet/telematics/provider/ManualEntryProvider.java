package com.evfleet.telematics.provider;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.telematics.dto.VehicleTelemetryData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fallback telemetry provider for manual data entry
 * Used when no OEM API or device is configured for a vehicle
 * Returns data from the vehicle's existing database fields
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class ManualEntryProvider implements TelemetryProvider {

    @Override
    public String getProviderId() {
        return "manual_entry";
    }

    @Override
    public String getProviderName() {
        return "Manual Entry";
    }

    @Override
    public Vehicle.TelemetrySource getSourceType() {
        return Vehicle.TelemetrySource.MANUAL;
    }

    @Override
    public boolean supports(Vehicle vehicle) {
        // This provider supports ALL vehicles as a fallback
        // Priority is lowest - only used when no other provider is available
        return true;
    }

    @Override
    public Optional<VehicleTelemetryData> fetchLatestData(Vehicle vehicle) {
        log.debug("Fetching manually entered data for vehicle: {}", vehicle.getId());

        // Build telemetry data from vehicle's existing database fields
        VehicleTelemetryData data = VehicleTelemetryData.builder()
            .vehicleId(vehicle.getId())
            .deviceId(String.valueOf(vehicle.getId()))
            .source(Vehicle.TelemetrySource.MANUAL)
            .providerName(getProviderName())
            .dataQuality(Vehicle.TelemetryDataQuality.MANUAL)
            .timestamp(vehicle.getLastTelemetryUpdate() != null ?
                vehicle.getLastTelemetryUpdate() : LocalDateTime.now())
            .isEstimated(true)
            // Location
            .latitude(vehicle.getLatitude())
            .longitude(vehicle.getLongitude())
            // Odometer
            .odometer(vehicle.getOdometer())
            // Battery data (for EVs)
            .batterySoc(vehicle.getCurrentBatterySoc())
            .batteryVoltage(vehicle.getBatteryCapacity() != null ?
                vehicle.getBatteryCapacity() * 3.7 : null) // Rough estimate
            // Fuel data (for ICE/Hybrid)
            .fuelLevel(vehicle.getFuelLevel())
            .fuelPercentage(vehicle.getFuelLevel() != null && vehicle.getFuelTankCapacity() != null ?
                (vehicle.getFuelLevel() / vehicle.getFuelTankCapacity()) * 100 : null)
            // Status
            .ignitionOn(vehicle.getStatus() == Vehicle.VehicleStatus.IN_TRIP ||
                       vehicle.getStatus() == Vehicle.VehicleStatus.CHARGING)
            .isCharging(vehicle.getStatus() == Vehicle.VehicleStatus.CHARGING)
            .chargingStatus(vehicle.getStatus() == Vehicle.VehicleStatus.CHARGING ?
                "CHARGING" : "NOT_CHARGING")
            .vehicleStatus(vehicle.getStatus().name())
            .notes("Data manually entered - not real-time")
            .build();

        // Only return data if we have at least some information
        if (data.getLatitude() != null || data.getOdometer() != null ||
            data.getBatterySoc() != null || data.getFuelLevel() != null) {
            return Optional.of(data);
        }

        log.warn("No manually entered data available for vehicle: {}", vehicle.getId());
        return Optional.empty();
    }

    @Override
    public List<VehicleTelemetryData> fetchHistoricalData(Vehicle vehicle, LocalDateTime start, LocalDateTime end) {
        // Manual entry provider doesn't store historical data
        // Historical tracking should be done by the application layer
        log.debug("Historical data not available for manual entry provider");
        return new ArrayList<>();
    }

    @Override
    public boolean testConnection() {
        // Manual entry provider is always "connected" since it reads from database
        return true;
    }

    @Override
    public int getUpdateIntervalSeconds() {
        // Manual updates happen irregularly when admin/driver enters data
        return 3600; // Assume hourly manual updates
    }

    @Override
    public boolean supportsRealTimeStreaming() {
        return false; // Manual entry is not real-time
    }

    @Override
    public List<String> getSupportedDataFields() {
        return List.of(
            "latitude", "longitude", "odometer",
            "batterySoc", "fuelLevel", "vehicleStatus"
        );
    }
}
