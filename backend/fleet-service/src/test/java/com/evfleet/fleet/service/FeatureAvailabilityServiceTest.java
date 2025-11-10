package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.AvailableFeaturesDTO;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FeatureAvailabilityService
 * Tests feature availability logic for different fuel types
 * 
 * @since 2.0.0 (PR 5: Vehicle CRUD API Updates)
 */
class FeatureAvailabilityServiceTest {

    private FeatureAvailabilityService service;

    @BeforeEach
    void setUp() {
        service = new FeatureAvailabilityService();
    }

    // ===== EV Vehicle Features Tests =====

    @Test
    void testBuildAvailableFeatures_ForEVVehicle_ShouldIncludeBatteryAndChargingFeatures() {
        // Given
        Vehicle vehicle = new Vehicle();
        vehicle.setFuelType(FuelType.EV);

        // When
        AvailableFeaturesDTO features = service.buildAvailableFeatures(vehicle);

        // Then
        assertNotNull(features);
        assertTrue(features.isBatteryTrackingAvailable());
        assertTrue(features.isChargingManagementAvailable());
        assertTrue(features.isEnergyAnalyticsAvailable());
        assertFalse(features.isFuelConsumptionAvailable());
        assertFalse(features.isFuelStationDiscoveryAvailable());
        assertFalse(features.isEngineDiagnosticsAvailable());
        
        // Verify feature list
        assertNotNull(features.getFeatures());
        assertTrue(features.getFeatures().contains("BATTERY_TRACKING"));
        assertTrue(features.getFeatures().contains("CHARGING_MANAGEMENT"));
        assertTrue(features.getFeatures().contains("ENERGY_ANALYTICS"));
        assertFalse(features.getFeatures().contains("FUEL_CONSUMPTION"));
        assertFalse(features.getFeatures().contains("ENGINE_DIAGNOSTICS"));
    }

    @Test
    void testBuildAvailableFeatures_ForEVFuelType_ShouldIncludeBatteryAndChargingFeatures() {
        // When
        AvailableFeaturesDTO features = service.buildAvailableFeatures(FuelType.EV);

        // Then
        assertNotNull(features);
        assertTrue(features.isBatteryTrackingAvailable());
        assertTrue(features.isChargingManagementAvailable());
        assertTrue(features.isEnergyAnalyticsAvailable());
        assertFalse(features.isFuelConsumptionAvailable());
    }

    // ===== ICE Vehicle Features Tests =====

    @Test
    void testBuildAvailableFeatures_ForICEVehicle_ShouldIncludeFuelAndEngineFeatures() {
        // Given
        Vehicle vehicle = new Vehicle();
        vehicle.setFuelType(FuelType.ICE);

        // When
        AvailableFeaturesDTO features = service.buildAvailableFeatures(vehicle);

        // Then
        assertNotNull(features);
        assertFalse(features.isBatteryTrackingAvailable());
        assertFalse(features.isChargingManagementAvailable());
        assertFalse(features.isEnergyAnalyticsAvailable());
        assertTrue(features.isFuelConsumptionAvailable());
        assertTrue(features.isFuelStationDiscoveryAvailable());
        assertTrue(features.isEngineDiagnosticsAvailable());
        
        // Verify feature list
        assertNotNull(features.getFeatures());
        assertFalse(features.getFeatures().contains("BATTERY_TRACKING"));
        assertFalse(features.getFeatures().contains("CHARGING_MANAGEMENT"));
        assertTrue(features.getFeatures().contains("FUEL_CONSUMPTION"));
        assertTrue(features.getFeatures().contains("ENGINE_DIAGNOSTICS"));
        assertTrue(features.getFeatures().contains("FUEL_STATION_DISCOVERY"));
    }

    @Test
    void testBuildAvailableFeatures_ForICEFuelType_ShouldIncludeFuelAndEngineFeatures() {
        // When
        AvailableFeaturesDTO features = service.buildAvailableFeatures(FuelType.ICE);

        // Then
        assertNotNull(features);
        assertFalse(features.isBatteryTrackingAvailable());
        assertTrue(features.isFuelConsumptionAvailable());
        assertTrue(features.isEngineDiagnosticsAvailable());
    }

    // ===== Hybrid Vehicle Features Tests =====

    @Test
    void testBuildAvailableFeatures_ForHybridVehicle_ShouldIncludeAllFeatures() {
        // Given
        Vehicle vehicle = new Vehicle();
        vehicle.setFuelType(FuelType.HYBRID);

        // When
        AvailableFeaturesDTO features = service.buildAvailableFeatures(vehicle);

        // Then
        assertNotNull(features);
        assertTrue(features.isBatteryTrackingAvailable());
        assertTrue(features.isChargingManagementAvailable());
        assertTrue(features.isEnergyAnalyticsAvailable());
        assertTrue(features.isFuelConsumptionAvailable());
        assertTrue(features.isFuelStationDiscoveryAvailable());
        assertTrue(features.isEngineDiagnosticsAvailable());
        
        // Verify feature list includes both EV and ICE features
        assertNotNull(features.getFeatures());
        assertTrue(features.getFeatures().contains("BATTERY_TRACKING"));
        assertTrue(features.getFeatures().contains("CHARGING_MANAGEMENT"));
        assertTrue(features.getFeatures().contains("ENERGY_ANALYTICS"));
        assertTrue(features.getFeatures().contains("FUEL_CONSUMPTION"));
        assertTrue(features.getFeatures().contains("ENGINE_DIAGNOSTICS"));
        assertTrue(features.getFeatures().contains("FUEL_STATION_DISCOVERY"));
        assertTrue(features.getFeatures().contains("HYBRID_MODE_OPTIMIZATION"));
    }

    @Test
    void testBuildAvailableFeatures_ForHybridFuelType_ShouldIncludeAllFeatures() {
        // When
        AvailableFeaturesDTO features = service.buildAvailableFeatures(FuelType.HYBRID);

        // Then
        assertNotNull(features);
        assertTrue(features.isBatteryTrackingAvailable());
        assertTrue(features.isChargingManagementAvailable());
        assertTrue(features.isFuelConsumptionAvailable());
        assertTrue(features.isEngineDiagnosticsAvailable());
    }

    // ===== Null/Default Tests =====

    @Test
    void testBuildAvailableFeatures_ForNullFuelType_ShouldDefaultToEVFeatures() {
        // Given
        Vehicle vehicle = new Vehicle();
        vehicle.setFuelType(null);

        // When
        AvailableFeaturesDTO features = service.buildAvailableFeatures(vehicle);

        // Then - should default to EV features for backward compatibility
        assertNotNull(features);
        assertTrue(features.isBatteryTrackingAvailable());
        assertTrue(features.isChargingManagementAvailable());
        assertFalse(features.isFuelConsumptionAvailable());
    }

    @Test
    void testBuildAvailableFeatures_WithNullFuelTypeEnum_ShouldDefaultToEVFeatures() {
        // When
        AvailableFeaturesDTO features = service.buildAvailableFeatures((FuelType) null);

        // Then
        assertNotNull(features);
        assertTrue(features.isBatteryTrackingAvailable());
        assertTrue(features.isChargingManagementAvailable());
        assertFalse(features.isFuelConsumptionAvailable());
    }

    // ===== Feature List Size Tests =====

    @Test
    void testBuildAvailableFeatures_EVFeatureListSize() {
        // When
        AvailableFeaturesDTO features = service.buildAvailableFeatures(FuelType.EV);

        // Then
        assertNotNull(features.getFeatures());
        assertEquals(5, features.getFeatures().size());
    }

    @Test
    void testBuildAvailableFeatures_ICEFeatureListSize() {
        // When
        AvailableFeaturesDTO features = service.buildAvailableFeatures(FuelType.ICE);

        // Then
        assertNotNull(features.getFeatures());
        assertEquals(4, features.getFeatures().size());
    }

    @Test
    void testBuildAvailableFeatures_HybridFeatureListSize() {
        // When
        AvailableFeaturesDTO features = service.buildAvailableFeatures(FuelType.HYBRID);

        // Then
        assertNotNull(features.getFeatures());
        assertEquals(10, features.getFeatures().size());
    }
}
