package com.evfleet.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing available features for a vehicle based on its fuel type
 * Different fuel types support different features:
 * - EV: Battery tracking, charging management, energy consumption
 * - ICE: Fuel consumption tracking, fuel station discovery
 * - HYBRID: All features from both EV and ICE
 * 
 * @since 2.0.0 (PR 5: Vehicle CRUD API Updates)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableFeaturesDTO {

    /**
     * List of feature names available for this vehicle
     * Examples: "BATTERY_TRACKING", "FUEL_CONSUMPTION", "CHARGING_MANAGEMENT"
     */
    private List<String> features;

    /**
     * Indicates if battery tracking is available
     */
    private boolean batteryTrackingAvailable;

    /**
     * Indicates if charging management is available
     */
    private boolean chargingManagementAvailable;

    /**
     * Indicates if fuel consumption tracking is available
     */
    private boolean fuelConsumptionAvailable;

    /**
     * Indicates if fuel station discovery is available
     */
    private boolean fuelStationDiscoveryAvailable;

    /**
     * Indicates if energy consumption analytics is available
     */
    private boolean energyAnalyticsAvailable;

    /**
     * Indicates if engine diagnostics is available
     */
    private boolean engineDiagnosticsAvailable;
}
