package com.evfleet.fleet.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FeatureToggle entity
 */
class FeatureToggleTest {

    private FeatureToggle featureToggle;

    @BeforeEach
    void setUp() {
        Set<FuelType> fuelTypes = new HashSet<>();
        fuelTypes.add(FuelType.EV);
        fuelTypes.add(FuelType.HYBRID);
        
        featureToggle = new FeatureToggle(
            FeatureToggle.Features.BATTERY_TRACKING,
            "Track battery state of charge and health",
            true,
            fuelTypes
        );
    }

    @Test
    void testFeatureToggleCreation() {
        assertNotNull(featureToggle);
        assertEquals(FeatureToggle.Features.BATTERY_TRACKING, featureToggle.getFeatureName());
        assertEquals("Track battery state of charge and health", featureToggle.getDescription());
        assertTrue(featureToggle.getEnabled());
        assertEquals(2, featureToggle.getSupportedFuelTypes().size());
    }

    @Test
    void testIsAvailableFor_EVVehicle_ShouldBeTrue() {
        assertTrue(featureToggle.isAvailableFor(FuelType.EV));
    }

    @Test
    void testIsAvailableFor_HybridVehicle_ShouldBeTrue() {
        assertTrue(featureToggle.isAvailableFor(FuelType.HYBRID));
    }

    @Test
    void testIsAvailableFor_ICEVehicle_ShouldBeFalse() {
        assertFalse(featureToggle.isAvailableFor(FuelType.ICE));
    }

    @Test
    void testIsAvailableFor_DisabledFeature_ShouldBeFalse() {
        featureToggle.setEnabled(false);
        assertFalse(featureToggle.isAvailableFor(FuelType.EV));
        assertFalse(featureToggle.isAvailableFor(FuelType.HYBRID));
        assertFalse(featureToggle.isAvailableFor(FuelType.ICE));
    }

    @Test
    void testFeatureConstants() {
        // Test that all feature constants are defined
        assertEquals("BATTERY_TRACKING", FeatureToggle.Features.BATTERY_TRACKING);
        assertEquals("CHARGING_MANAGEMENT", FeatureToggle.Features.CHARGING_MANAGEMENT);
        assertEquals("FUEL_CONSUMPTION", FeatureToggle.Features.FUEL_CONSUMPTION);
        assertEquals("ENGINE_DIAGNOSTICS", FeatureToggle.Features.ENGINE_DIAGNOSTICS);
        assertEquals("ENERGY_OPTIMIZATION", FeatureToggle.Features.ENERGY_OPTIMIZATION);
        assertEquals("CARBON_FOOTPRINT", FeatureToggle.Features.CARBON_FOOTPRINT);
        assertEquals("RANGE_PREDICTION", FeatureToggle.Features.RANGE_PREDICTION);
        assertEquals("REGENERATIVE_BRAKING", FeatureToggle.Features.REGENERATIVE_BRAKING);
        assertEquals("BATTERY_HEALTH", FeatureToggle.Features.BATTERY_HEALTH);
        assertEquals("FUEL_STATION_DISCOVERY", FeatureToggle.Features.FUEL_STATION_DISCOVERY);
        assertEquals("CHARGING_STATION_DISCOVERY", FeatureToggle.Features.CHARGING_STATION_DISCOVERY);
        assertEquals("TRIP_COST_ANALYSIS", FeatureToggle.Features.TRIP_COST_ANALYSIS);
    }

    @Test
    void testAddFuelType() {
        Set<FuelType> fuelTypes = new HashSet<>();
        fuelTypes.add(FuelType.ICE);
        
        FeatureToggle fuelFeature = new FeatureToggle(
            FeatureToggle.Features.FUEL_CONSUMPTION,
            "Track fuel consumption",
            true,
            fuelTypes
        );
        
        // Initially only ICE
        assertTrue(fuelFeature.isAvailableFor(FuelType.ICE));
        assertFalse(fuelFeature.isAvailableFor(FuelType.EV));
        
        // Add HYBRID support
        fuelFeature.getSupportedFuelTypes().add(FuelType.HYBRID);
        assertTrue(fuelFeature.isAvailableFor(FuelType.HYBRID));
    }

    @Test
    void testRemoveFuelType() {
        // Initially supports EV and HYBRID
        assertTrue(featureToggle.isAvailableFor(FuelType.EV));
        assertTrue(featureToggle.isAvailableFor(FuelType.HYBRID));
        
        // Remove EV support
        featureToggle.getSupportedFuelTypes().remove(FuelType.EV);
        assertFalse(featureToggle.isAvailableFor(FuelType.EV));
        assertTrue(featureToggle.isAvailableFor(FuelType.HYBRID));
    }

    @Test
    void testCommonFeature_AllFuelTypes() {
        Set<FuelType> allFuelTypes = new HashSet<>();
        allFuelTypes.add(FuelType.EV);
        allFuelTypes.add(FuelType.ICE);
        allFuelTypes.add(FuelType.HYBRID);
        
        FeatureToggle commonFeature = new FeatureToggle(
            FeatureToggle.Features.CARBON_FOOTPRINT,
            "Calculate carbon emissions",
            true,
            allFuelTypes
        );
        
        // Should be available for all fuel types
        assertTrue(commonFeature.isAvailableFor(FuelType.EV));
        assertTrue(commonFeature.isAvailableFor(FuelType.ICE));
        assertTrue(commonFeature.isAvailableFor(FuelType.HYBRID));
    }

    @Test
    void testEmptySupportedFuelTypes() {
        FeatureToggle emptyFeature = new FeatureToggle(
            "TEST_FEATURE",
            "Test feature with no fuel types",
            true,
            new HashSet<>()
        );
        
        // Should not be available for any fuel type
        assertFalse(emptyFeature.isAvailableFor(FuelType.EV));
        assertFalse(emptyFeature.isAvailableFor(FuelType.ICE));
        assertFalse(emptyFeature.isAvailableFor(FuelType.HYBRID));
    }

    @Test
    void testDefaultConstructor() {
        FeatureToggle defaultFeature = new FeatureToggle();
        assertNull(defaultFeature.getFeatureName());
        assertNull(defaultFeature.getDescription());
        assertNotNull(defaultFeature.getSupportedFuelTypes());
        assertTrue(defaultFeature.getSupportedFuelTypes().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        FeatureToggle feature = new FeatureToggle();
        
        feature.setId(1L);
        feature.setFeatureName("TEST_FEATURE");
        feature.setDescription("Test description");
        feature.setEnabled(false);
        
        Set<FuelType> fuelTypes = new HashSet<>();
        fuelTypes.add(FuelType.EV);
        feature.setSupportedFuelTypes(fuelTypes);
        
        assertEquals(1L, feature.getId());
        assertEquals("TEST_FEATURE", feature.getFeatureName());
        assertEquals("Test description", feature.getDescription());
        assertFalse(feature.getEnabled());
        assertEquals(1, feature.getSupportedFuelTypes().size());
        assertTrue(feature.getSupportedFuelTypes().contains(FuelType.EV));
    }
}
