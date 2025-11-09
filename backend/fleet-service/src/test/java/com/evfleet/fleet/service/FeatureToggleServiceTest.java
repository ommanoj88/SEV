package com.evfleet.fleet.service;

import com.evfleet.fleet.exception.FeatureNotAvailableException;
import com.evfleet.fleet.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.FeatureToggle;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.FeatureToggleRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.fleet.service.impl.FeatureToggleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FeatureToggleService
 */
@ExtendWith(MockitoExtension.class)
class FeatureToggleServiceTest {

    @Mock
    private FeatureToggleRepository featureToggleRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private FeatureToggleServiceImpl featureToggleService;

    private FeatureToggle batteryTrackingFeature;
    private FeatureToggle fuelConsumptionFeature;
    private FeatureToggle carbonFootprintFeature;
    private Vehicle evVehicle;
    private Vehicle iceVehicle;
    private Vehicle hybridVehicle;

    @BeforeEach
    void setUp() {
        // Setup battery tracking feature (EV and HYBRID)
        Set<FuelType> evHybridTypes = new HashSet<>();
        evHybridTypes.add(FuelType.EV);
        evHybridTypes.add(FuelType.HYBRID);
        batteryTrackingFeature = new FeatureToggle(
            FeatureToggle.Features.BATTERY_TRACKING,
            "Track battery health",
            true,
            evHybridTypes
        );

        // Setup fuel consumption feature (ICE and HYBRID)
        Set<FuelType> iceHybridTypes = new HashSet<>();
        iceHybridTypes.add(FuelType.ICE);
        iceHybridTypes.add(FuelType.HYBRID);
        fuelConsumptionFeature = new FeatureToggle(
            FeatureToggle.Features.FUEL_CONSUMPTION,
            "Track fuel consumption",
            true,
            iceHybridTypes
        );

        // Setup carbon footprint feature (all types)
        Set<FuelType> allTypes = new HashSet<>();
        allTypes.add(FuelType.EV);
        allTypes.add(FuelType.ICE);
        allTypes.add(FuelType.HYBRID);
        carbonFootprintFeature = new FeatureToggle(
            FeatureToggle.Features.CARBON_FOOTPRINT,
            "Calculate carbon footprint",
            true,
            allTypes
        );

        // Setup test vehicles
        evVehicle = new Vehicle();
        evVehicle.setId(1L);
        evVehicle.setFuelType(FuelType.EV);

        iceVehicle = new Vehicle();
        iceVehicle.setId(2L);
        iceVehicle.setFuelType(FuelType.ICE);

        hybridVehicle = new Vehicle();
        hybridVehicle.setId(3L);
        hybridVehicle.setFuelType(FuelType.HYBRID);
    }

    @Test
    void testIsFeatureAvailable_EVBatteryTracking_ShouldBeTrue() {
        when(featureToggleRepository.isFeatureAvailableForFuelType(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.EV))
            .thenReturn(true);

        boolean result = featureToggleService.isFeatureAvailable(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.EV);

        assertTrue(result);
        verify(featureToggleRepository).isFeatureAvailableForFuelType(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.EV);
    }

    @Test
    void testIsFeatureAvailable_ICEBatteryTracking_ShouldBeFalse() {
        when(featureToggleRepository.isFeatureAvailableForFuelType(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.ICE))
            .thenReturn(false);

        boolean result = featureToggleService.isFeatureAvailable(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.ICE);

        assertFalse(result);
        verify(featureToggleRepository).isFeatureAvailableForFuelType(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.ICE);
    }

    @Test
    void testIsFeatureAvailableForVehicle_EVVehicle_ShouldBeTrue() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(evVehicle));
        when(featureToggleRepository.isFeatureAvailableForFuelType(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.EV))
            .thenReturn(true);

        boolean result = featureToggleService.isFeatureAvailableForVehicle(
            FeatureToggle.Features.BATTERY_TRACKING, 1L);

        assertTrue(result);
        verify(vehicleRepository).findById(1L);
        verify(featureToggleRepository).isFeatureAvailableForFuelType(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.EV);
    }

    @Test
    void testIsFeatureAvailableForVehicle_VehicleNotFound_ShouldThrowException() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            featureToggleService.isFeatureAvailableForVehicle(
                FeatureToggle.Features.BATTERY_TRACKING, 999L);
        });

        verify(vehicleRepository).findById(999L);
        verify(featureToggleRepository, never()).isFeatureAvailableForFuelType(anyString(), any());
    }

    @Test
    void testIsFeatureAvailableForVehicle_VehicleWithNullFuelType_ShouldDefaultToEV() {
        Vehicle vehicleWithoutFuelType = new Vehicle();
        vehicleWithoutFuelType.setId(4L);
        vehicleWithoutFuelType.setFuelType(null);

        when(vehicleRepository.findById(4L)).thenReturn(Optional.of(vehicleWithoutFuelType));
        when(featureToggleRepository.isFeatureAvailableForFuelType(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.EV))
            .thenReturn(true);

        boolean result = featureToggleService.isFeatureAvailableForVehicle(
            FeatureToggle.Features.BATTERY_TRACKING, 4L);

        assertTrue(result);
        verify(featureToggleRepository).isFeatureAvailableForFuelType(
            FeatureToggle.Features.BATTERY_TRACKING, FuelType.EV);
    }

    @Test
    void testGetAvailableFeatures_EVVehicle() {
        List<FeatureToggle> evFeatures = Arrays.asList(
            batteryTrackingFeature,
            carbonFootprintFeature
        );

        when(featureToggleRepository.findByFuelType(FuelType.EV))
            .thenReturn(evFeatures);

        List<String> result = featureToggleService.getAvailableFeatures(FuelType.EV);

        assertEquals(2, result.size());
        assertTrue(result.contains(FeatureToggle.Features.BATTERY_TRACKING));
        assertTrue(result.contains(FeatureToggle.Features.CARBON_FOOTPRINT));
        verify(featureToggleRepository).findByFuelType(FuelType.EV);
    }

    @Test
    void testGetAvailableFeatures_ICEVehicle() {
        List<FeatureToggle> iceFeatures = Arrays.asList(
            fuelConsumptionFeature,
            carbonFootprintFeature
        );

        when(featureToggleRepository.findByFuelType(FuelType.ICE))
            .thenReturn(iceFeatures);

        List<String> result = featureToggleService.getAvailableFeatures(FuelType.ICE);

        assertEquals(2, result.size());
        assertTrue(result.contains(FeatureToggle.Features.FUEL_CONSUMPTION));
        assertTrue(result.contains(FeatureToggle.Features.CARBON_FOOTPRINT));
        verify(featureToggleRepository).findByFuelType(FuelType.ICE);
    }

    @Test
    void testGetAvailableFeatures_HybridVehicle() {
        List<FeatureToggle> hybridFeatures = Arrays.asList(
            batteryTrackingFeature,
            fuelConsumptionFeature,
            carbonFootprintFeature
        );

        when(featureToggleRepository.findByFuelType(FuelType.HYBRID))
            .thenReturn(hybridFeatures);

        List<String> result = featureToggleService.getAvailableFeatures(FuelType.HYBRID);

        assertEquals(3, result.size());
        assertTrue(result.contains(FeatureToggle.Features.BATTERY_TRACKING));
        assertTrue(result.contains(FeatureToggle.Features.FUEL_CONSUMPTION));
        assertTrue(result.contains(FeatureToggle.Features.CARBON_FOOTPRINT));
        verify(featureToggleRepository).findByFuelType(FuelType.HYBRID);
    }

    @Test
    void testGetAvailableFeaturesForVehicle() {
        List<FeatureToggle> evFeatures = Arrays.asList(
            batteryTrackingFeature,
            carbonFootprintFeature
        );

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(evVehicle));
        when(featureToggleRepository.findByFuelType(FuelType.EV))
            .thenReturn(evFeatures);

        List<String> result = featureToggleService.getAvailableFeaturesForVehicle(1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(FeatureToggle.Features.BATTERY_TRACKING));
        assertTrue(result.contains(FeatureToggle.Features.CARBON_FOOTPRINT));
    }

    @Test
    void testGetFeatureToggle_Exists() {
        when(featureToggleRepository.findByFeatureName(FeatureToggle.Features.BATTERY_TRACKING))
            .thenReturn(Optional.of(batteryTrackingFeature));

        FeatureToggle result = featureToggleService.getFeatureToggle(
            FeatureToggle.Features.BATTERY_TRACKING);

        assertNotNull(result);
        assertEquals(FeatureToggle.Features.BATTERY_TRACKING, result.getFeatureName());
        verify(featureToggleRepository).findByFeatureName(FeatureToggle.Features.BATTERY_TRACKING);
    }

    @Test
    void testGetFeatureToggle_NotExists_ShouldThrowException() {
        when(featureToggleRepository.findByFeatureName("INVALID_FEATURE"))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            featureToggleService.getFeatureToggle("INVALID_FEATURE");
        });

        verify(featureToggleRepository).findByFeatureName("INVALID_FEATURE");
    }

    @Test
    void testGetAllFeatureToggles() {
        List<FeatureToggle> allFeatures = Arrays.asList(
            batteryTrackingFeature,
            fuelConsumptionFeature,
            carbonFootprintFeature
        );

        when(featureToggleRepository.findAll()).thenReturn(allFeatures);

        List<FeatureToggle> result = featureToggleService.getAllFeatureToggles();

        assertEquals(3, result.size());
        verify(featureToggleRepository).findAll();
    }

    @Test
    void testGetEnabledFeatureToggles() {
        List<FeatureToggle> enabledFeatures = Arrays.asList(
            batteryTrackingFeature,
            carbonFootprintFeature
        );

        when(featureToggleRepository.findByEnabledTrue()).thenReturn(enabledFeatures);

        List<FeatureToggle> result = featureToggleService.getEnabledFeatureToggles();

        assertEquals(2, result.size());
        verify(featureToggleRepository).findByEnabledTrue();
    }

    @Test
    void testSaveFeatureToggle() {
        when(featureToggleRepository.save(batteryTrackingFeature))
            .thenReturn(batteryTrackingFeature);

        FeatureToggle result = featureToggleService.saveFeatureToggle(batteryTrackingFeature);

        assertNotNull(result);
        assertEquals(FeatureToggle.Features.BATTERY_TRACKING, result.getFeatureName());
        verify(featureToggleRepository).save(batteryTrackingFeature);
    }

    @Test
    void testEnableFeature() {
        FeatureToggle disabledFeature = new FeatureToggle(
            FeatureToggle.Features.BATTERY_TRACKING,
            "Battery tracking",
            false,
            new HashSet<>(Arrays.asList(FuelType.EV))
        );

        when(featureToggleRepository.findByFeatureName(FeatureToggle.Features.BATTERY_TRACKING))
            .thenReturn(Optional.of(disabledFeature));
        when(featureToggleRepository.save(any(FeatureToggle.class)))
            .thenReturn(disabledFeature);

        featureToggleService.enableFeature(FeatureToggle.Features.BATTERY_TRACKING);

        assertTrue(disabledFeature.getEnabled());
        verify(featureToggleRepository).save(disabledFeature);
    }

    @Test
    void testDisableFeature() {
        when(featureToggleRepository.findByFeatureName(FeatureToggle.Features.BATTERY_TRACKING))
            .thenReturn(Optional.of(batteryTrackingFeature));
        when(featureToggleRepository.save(any(FeatureToggle.class)))
            .thenReturn(batteryTrackingFeature);

        featureToggleService.disableFeature(FeatureToggle.Features.BATTERY_TRACKING);

        assertFalse(batteryTrackingFeature.getEnabled());
        verify(featureToggleRepository).save(batteryTrackingFeature);
    }
}
