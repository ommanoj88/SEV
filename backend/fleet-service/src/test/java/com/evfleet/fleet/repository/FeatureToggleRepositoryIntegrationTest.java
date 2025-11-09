package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.FeatureToggle;
import com.evfleet.fleet.model.FuelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FeatureToggleRepository
 * Tests database operations and custom queries
 */
@DataJpaTest
@ActiveProfiles("test")
class FeatureToggleRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FeatureToggleRepository featureToggleRepository;

    private FeatureToggle batteryTracking;
    private FeatureToggle fuelConsumption;
    private FeatureToggle carbonFootprint;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        featureToggleRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Setup battery tracking (EV and HYBRID)
        Set<FuelType> evHybridTypes = new HashSet<>();
        evHybridTypes.add(FuelType.EV);
        evHybridTypes.add(FuelType.HYBRID);
        batteryTracking = new FeatureToggle(
            FeatureToggle.Features.BATTERY_TRACKING,
            "Track battery health",
            true,
            evHybridTypes
        );

        // Setup fuel consumption (ICE and HYBRID)
        Set<FuelType> iceHybridTypes = new HashSet<>();
        iceHybridTypes.add(FuelType.ICE);
        iceHybridTypes.add(FuelType.HYBRID);
        fuelConsumption = new FeatureToggle(
            FeatureToggle.Features.FUEL_CONSUMPTION,
            "Track fuel consumption",
            true,
            iceHybridTypes
        );

        // Setup carbon footprint (all types)
        Set<FuelType> allTypes = new HashSet<>();
        allTypes.add(FuelType.EV);
        allTypes.add(FuelType.ICE);
        allTypes.add(FuelType.HYBRID);
        carbonFootprint = new FeatureToggle(
            FeatureToggle.Features.CARBON_FOOTPRINT,
            "Calculate carbon footprint",
            true,
            allTypes
        );

        // Save features
        batteryTracking = featureToggleRepository.save(batteryTracking);
        fuelConsumption = featureToggleRepository.save(fuelConsumption);
        carbonFootprint = featureToggleRepository.save(carbonFootprint);
        
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testSaveAndRetrieveFeatureToggle() {
        // Given a new feature
        Set<FuelType> evTypes = new HashSet<>();
        evTypes.add(FuelType.EV);
        FeatureToggle newFeature = new FeatureToggle(
            "TEST_FEATURE",
            "Test feature",
            true,
            evTypes
        );

        // When saved
        FeatureToggle saved = featureToggleRepository.save(newFeature);
        entityManager.flush();
        entityManager.clear();

        // Then it can be retrieved
        Optional<FeatureToggle> retrieved = featureToggleRepository.findById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("TEST_FEATURE", retrieved.get().getFeatureName());
        assertEquals(1, retrieved.get().getSupportedFuelTypes().size());
        assertTrue(retrieved.get().getSupportedFuelTypes().contains(FuelType.EV));
    }

    @Test
    void testFindByFeatureName() {
        // When searching by feature name
        Optional<FeatureToggle> found = featureToggleRepository.findByFeatureName(
            FeatureToggle.Features.BATTERY_TRACKING);

        // Then the feature is found
        assertTrue(found.isPresent());
        assertEquals(FeatureToggle.Features.BATTERY_TRACKING, found.get().getFeatureName());
        assertEquals(2, found.get().getSupportedFuelTypes().size());
    }

    @Test
    void testFindByFeatureName_NotFound() {
        // When searching for non-existent feature
        Optional<FeatureToggle> found = featureToggleRepository.findByFeatureName("NON_EXISTENT");

        // Then no result is returned
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEnabledTrue() {
        // All features are enabled
        List<FeatureToggle> enabledFeatures = featureToggleRepository.findByEnabledTrue();

        // Then all features are returned
        assertEquals(3, enabledFeatures.size());
    }

    @Test
    void testFindByEnabledTrue_WithDisabledFeature() {
        // Given a disabled feature
        batteryTracking.setEnabled(false);
        featureToggleRepository.save(batteryTracking);
        entityManager.flush();

        // When searching for enabled features
        List<FeatureToggle> enabledFeatures = featureToggleRepository.findByEnabledTrue();

        // Then only enabled features are returned
        assertEquals(2, enabledFeatures.size());
        assertFalse(enabledFeatures.stream()
            .anyMatch(f -> f.getFeatureName().equals(FeatureToggle.Features.BATTERY_TRACKING)));
    }

    @Test
    void testFindByFuelType_EV() {
        // When searching for EV features
        List<FeatureToggle> evFeatures = featureToggleRepository.findByFuelType(FuelType.EV);

        // Then features supporting EV are returned
        assertEquals(2, evFeatures.size());
        assertTrue(evFeatures.stream()
            .anyMatch(f -> f.getFeatureName().equals(FeatureToggle.Features.BATTERY_TRACKING)));
        assertTrue(evFeatures.stream()
            .anyMatch(f -> f.getFeatureName().equals(FeatureToggle.Features.CARBON_FOOTPRINT)));
    }

    @Test
    void testFindByFuelType_ICE() {
        // When searching for ICE features
        List<FeatureToggle> iceFeatures = featureToggleRepository.findByFuelType(FuelType.ICE);

        // Then features supporting ICE are returned
        assertEquals(2, iceFeatures.size());
        assertTrue(iceFeatures.stream()
            .anyMatch(f -> f.getFeatureName().equals(FeatureToggle.Features.FUEL_CONSUMPTION)));
        assertTrue(iceFeatures.stream()
            .anyMatch(f -> f.getFeatureName().equals(FeatureToggle.Features.CARBON_FOOTPRINT)));
    }

    @Test
    void testFindByFuelType_HYBRID() {
        // When searching for HYBRID features
        List<FeatureToggle> hybridFeatures = featureToggleRepository.findByFuelType(FuelType.HYBRID);

        // Then all features are returned (all support HYBRID)
        assertEquals(3, hybridFeatures.size());
    }

    @Test
    void testIsFeatureAvailableForFuelType_Available() {
        // When checking if battery tracking is available for EV
        boolean available = featureToggleRepository.isFeatureAvailableForFuelType(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.EV);

        // Then it is available
        assertTrue(available);
    }

    @Test
    void testIsFeatureAvailableForFuelType_NotAvailable() {
        // When checking if battery tracking is available for ICE
        boolean available = featureToggleRepository.isFeatureAvailableForFuelType(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.ICE);

        // Then it is not available
        assertFalse(available);
    }

    @Test
    void testIsFeatureAvailableForFuelType_DisabledFeature() {
        // Given a disabled feature
        batteryTracking.setEnabled(false);
        featureToggleRepository.save(batteryTracking);
        entityManager.flush();

        // When checking availability
        boolean available = featureToggleRepository.isFeatureAvailableForFuelType(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.EV);

        // Then it is not available
        assertFalse(available);
    }

    @Test
    void testExistsByFeatureName_Exists() {
        // When checking if feature exists
        boolean exists = featureToggleRepository.existsByFeatureName(
            FeatureToggle.Features.BATTERY_TRACKING);

        // Then it exists
        assertTrue(exists);
    }

    @Test
    void testExistsByFeatureName_NotExists() {
        // When checking if non-existent feature exists
        boolean exists = featureToggleRepository.existsByFeatureName("NON_EXISTENT");

        // Then it doesn't exist
        assertFalse(exists);
    }

    @Test
    void testDeleteFeatureToggle() {
        // Given a feature toggle
        Long id = batteryTracking.getId();

        // When deleted
        featureToggleRepository.deleteById(id);
        entityManager.flush();

        // Then it no longer exists
        Optional<FeatureToggle> deleted = featureToggleRepository.findById(id);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testUpdateFeatureToggle() {
        // Given an existing feature
        batteryTracking.setDescription("Updated description");
        batteryTracking.setEnabled(false);

        // When updated
        FeatureToggle updated = featureToggleRepository.save(batteryTracking);
        entityManager.flush();
        entityManager.clear();

        // Then changes are persisted
        Optional<FeatureToggle> retrieved = featureToggleRepository.findById(updated.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("Updated description", retrieved.get().getDescription());
        assertFalse(retrieved.get().getEnabled());
    }

    @Test
    void testAddFuelTypeToFeature() {
        // Given battery tracking doesn't support ICE
        assertFalse(batteryTracking.getSupportedFuelTypes().contains(FuelType.ICE));

        // When ICE is added
        batteryTracking.getSupportedFuelTypes().add(FuelType.ICE);
        featureToggleRepository.save(batteryTracking);
        entityManager.flush();
        entityManager.clear();

        // Then ICE is supported
        Optional<FeatureToggle> retrieved = featureToggleRepository.findById(batteryTracking.getId());
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get().getSupportedFuelTypes().contains(FuelType.ICE));
        assertEquals(3, retrieved.get().getSupportedFuelTypes().size());
    }

    @Test
    void testRemoveFuelTypeFromFeature() {
        // Given carbon footprint supports all fuel types
        assertEquals(3, carbonFootprint.getSupportedFuelTypes().size());

        // When ICE is removed
        carbonFootprint.getSupportedFuelTypes().remove(FuelType.ICE);
        featureToggleRepository.save(carbonFootprint);
        entityManager.flush();
        entityManager.clear();

        // Then ICE is no longer supported
        Optional<FeatureToggle> retrieved = featureToggleRepository.findById(carbonFootprint.getId());
        assertTrue(retrieved.isPresent());
        assertFalse(retrieved.get().getSupportedFuelTypes().contains(FuelType.ICE));
        assertEquals(2, retrieved.get().getSupportedFuelTypes().size());
    }
}
