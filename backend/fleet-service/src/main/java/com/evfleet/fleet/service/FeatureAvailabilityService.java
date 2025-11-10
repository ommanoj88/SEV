package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.AvailableFeaturesDTO;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for determining available features for vehicles based on fuel type
 * 
 * @since 2.0.0 (PR 5: Vehicle CRUD API Updates)
 */
@Service
public class FeatureAvailabilityService {

    /**
     * Build available features DTO based on vehicle's fuel type
     * 
     * @param vehicle The vehicle to build features for
     * @return AvailableFeaturesDTO with appropriate features enabled
     */
    public AvailableFeaturesDTO buildAvailableFeatures(Vehicle vehicle) {
        if (vehicle.getFuelType() == null) {
            // Default to EV features for backward compatibility
            return buildEVFeatures();
        }

        switch (vehicle.getFuelType()) {
            case EV:
                return buildEVFeatures();
            case ICE:
                return buildICEFeatures();
            case HYBRID:
                return buildHybridFeatures();
            default:
                return buildEVFeatures(); // Default fallback
        }
    }

    /**
     * Build features for EV vehicles
     * EVs support battery tracking, charging, and energy analytics
     */
    private AvailableFeaturesDTO buildEVFeatures() {
        List<String> features = new ArrayList<>();
        features.add("BATTERY_TRACKING");
        features.add("CHARGING_MANAGEMENT");
        features.add("ENERGY_ANALYTICS");
        features.add("RANGE_PREDICTION");
        features.add("CHARGING_STATION_DISCOVERY");

        return AvailableFeaturesDTO.builder()
                .features(features)
                .batteryTrackingAvailable(true)
                .chargingManagementAvailable(true)
                .fuelConsumptionAvailable(false)
                .fuelStationDiscoveryAvailable(false)
                .energyAnalyticsAvailable(true)
                .engineDiagnosticsAvailable(false)
                .build();
    }

    /**
     * Build features for ICE vehicles
     * ICE vehicles support fuel consumption tracking and fuel station discovery
     */
    private AvailableFeaturesDTO buildICEFeatures() {
        List<String> features = new ArrayList<>();
        features.add("FUEL_CONSUMPTION");
        features.add("FUEL_STATION_DISCOVERY");
        features.add("ENGINE_DIAGNOSTICS");
        features.add("MAINTENANCE_SCHEDULING");

        return AvailableFeaturesDTO.builder()
                .features(features)
                .batteryTrackingAvailable(false)
                .chargingManagementAvailable(false)
                .fuelConsumptionAvailable(true)
                .fuelStationDiscoveryAvailable(true)
                .energyAnalyticsAvailable(false)
                .engineDiagnosticsAvailable(true)
                .build();
    }

    /**
     * Build features for Hybrid vehicles
     * Hybrid vehicles support all features from both EV and ICE
     */
    private AvailableFeaturesDTO buildHybridFeatures() {
        List<String> features = new ArrayList<>();
        features.add("BATTERY_TRACKING");
        features.add("CHARGING_MANAGEMENT");
        features.add("ENERGY_ANALYTICS");
        features.add("RANGE_PREDICTION");
        features.add("CHARGING_STATION_DISCOVERY");
        features.add("FUEL_CONSUMPTION");
        features.add("FUEL_STATION_DISCOVERY");
        features.add("ENGINE_DIAGNOSTICS");
        features.add("MAINTENANCE_SCHEDULING");
        features.add("HYBRID_MODE_OPTIMIZATION");

        return AvailableFeaturesDTO.builder()
                .features(features)
                .batteryTrackingAvailable(true)
                .chargingManagementAvailable(true)
                .fuelConsumptionAvailable(true)
                .fuelStationDiscoveryAvailable(true)
                .energyAnalyticsAvailable(true)
                .engineDiagnosticsAvailable(true)
                .build();
    }

    /**
     * Build features based on fuel type enum
     * 
     * @param fuelType The fuel type
     * @return AvailableFeaturesDTO with appropriate features
     */
    public AvailableFeaturesDTO buildAvailableFeatures(FuelType fuelType) {
        if (fuelType == null) {
            return buildEVFeatures();
        }

        switch (fuelType) {
            case EV:
                return buildEVFeatures();
            case ICE:
                return buildICEFeatures();
            case HYBRID:
                return buildHybridFeatures();
            default:
                return buildEVFeatures();
        }
    }
}
