package com.evfleet.telematics.provider;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.telematics.dto.VehicleTelemetryData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface for all telemetry providers (OEM APIs, devices, etc.)
 * Each provider must implement these methods to integrate with the unified system
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
public interface TelemetryProvider {

    /**
     * Get the unique identifier for this provider
     * Examples: "tata_fleetedge", "mg_ismart", "teltonika_fmc003"
     */
    String getProviderId();

    /**
     * Get human-readable name
     * Examples: "Tata FleetEdge", "MG iSmart", "Teltonika FMC003"
     */
    String getProviderName();

    /**
     * Get the source type this provider belongs to
     */
    Vehicle.TelemetrySource getSourceType();

    /**
     * Check if this provider can handle a specific vehicle
     */
    boolean supports(Vehicle vehicle);

    /**
     * Fetch the latest telemetry data for a vehicle
     * @param vehicle The vehicle to fetch data for
     * @return Latest telemetry data, or empty if unavailable
     */
    Optional<VehicleTelemetryData> fetchLatestData(Vehicle vehicle);

    /**
     * Fetch historical telemetry data within a time range
     * @param vehicle The vehicle to fetch data for
     * @param start Start time
     * @param end End time
     * @return List of telemetry data points
     */
    List<VehicleTelemetryData> fetchHistoricalData(Vehicle vehicle, LocalDateTime start, LocalDateTime end);

    /**
     * Test connectivity with the provider
     * @return true if provider is accessible, false otherwise
     */
    boolean testConnection();

    /**
     * Register a vehicle with this provider (if needed)
     * @param vehicle The vehicle to register
     * @return true if registration successful
     */
    default boolean registerVehicle(Vehicle vehicle) {
        return true; // Default implementation does nothing
    }

    /**
     * Unregister a vehicle from this provider
     * @param vehicle The vehicle to unregister
     * @return true if unregistration successful
     */
    default boolean unregisterVehicle(Vehicle vehicle) {
        return true; // Default implementation does nothing
    }

    /**
     * Get the expected data update frequency for this provider
     * @return Update interval in seconds (e.g., 30 for every 30 seconds)
     */
    int getUpdateIntervalSeconds();

    /**
     * Check if this provider supports real-time streaming
     */
    default boolean supportsRealTimeStreaming() {
        return false;
    }

    /**
     * Get supported data fields for this provider
     */
    List<String> getSupportedDataFields();
}
