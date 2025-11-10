package com.evfleet.fleet.controller;

import com.evfleet.fleet.dto.AvailableFeaturesDTO;
import com.evfleet.fleet.dto.VehicleResponse;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.service.FeatureAvailabilityService;
import com.evfleet.fleet.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VehicleController
 * Tests the available features endpoint added in PR 8
 * 
 * @since 2.0.0 (PR 8: Feature Availability in APIs)
 */
@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock
    private VehicleService vehicleService;

    @Mock
    private FeatureAvailabilityService featureAvailabilityService;

    @InjectMocks
    private VehicleController vehicleController;

    private VehicleResponse evVehicleResponse;
    private VehicleResponse iceVehicleResponse;
    private VehicleResponse hybridVehicleResponse;

    @BeforeEach
    void setUp() {
        // Setup EV vehicle response
        evVehicleResponse = new VehicleResponse();
        evVehicleResponse.setId(1L);
        evVehicleResponse.setVehicleNumber("EV-001");
        evVehicleResponse.setFuelType(FuelType.EV);
        evVehicleResponse.setBatteryCapacity(75.0);

        // Setup ICE vehicle response
        iceVehicleResponse = new VehicleResponse();
        iceVehicleResponse.setId(2L);
        iceVehicleResponse.setVehicleNumber("ICE-001");
        iceVehicleResponse.setFuelType(FuelType.ICE);
        iceVehicleResponse.setFuelTankCapacity(50.0);

        // Setup Hybrid vehicle response
        hybridVehicleResponse = new VehicleResponse();
        hybridVehicleResponse.setId(3L);
        hybridVehicleResponse.setVehicleNumber("HYB-001");
        hybridVehicleResponse.setFuelType(FuelType.HYBRID);
        hybridVehicleResponse.setBatteryCapacity(50.0);
        hybridVehicleResponse.setFuelTankCapacity(40.0);
    }

    // ===== PR 8: Available Features Endpoint Tests =====

    @Test
    void testGetAvailableFeatures_ForEVVehicle_ShouldReturnEVFeatures() {
        // Given
        Long vehicleId = 1L;
        List<String> evFeatures = Arrays.asList(
            "BATTERY_TRACKING", 
            "CHARGING_MANAGEMENT", 
            "ENERGY_ANALYTICS", 
            "RANGE_PREDICTION", 
            "CHARGING_STATION_DISCOVERY"
        );
        AvailableFeaturesDTO evFeaturesDTO = AvailableFeaturesDTO.builder()
                .features(evFeatures)
                .batteryTrackingAvailable(true)
                .chargingManagementAvailable(true)
                .energyAnalyticsAvailable(true)
                .fuelConsumptionAvailable(false)
                .fuelStationDiscoveryAvailable(false)
                .engineDiagnosticsAvailable(false)
                .build();

        when(vehicleService.getVehicleById(vehicleId)).thenReturn(evVehicleResponse);
        when(featureAvailabilityService.buildAvailableFeatures(any(Vehicle.class))).thenReturn(evFeaturesDTO);

        // When
        ResponseEntity<AvailableFeaturesDTO> response = vehicleController.getAvailableFeatures(vehicleId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        AvailableFeaturesDTO features = response.getBody();
        assertTrue(features.isBatteryTrackingAvailable());
        assertTrue(features.isChargingManagementAvailable());
        assertTrue(features.isEnergyAnalyticsAvailable());
        assertFalse(features.isFuelConsumptionAvailable());
        assertFalse(features.isFuelStationDiscoveryAvailable());
        assertFalse(features.isEngineDiagnosticsAvailable());
        
        assertEquals(5, features.getFeatures().size());
        assertTrue(features.getFeatures().contains("BATTERY_TRACKING"));
        assertTrue(features.getFeatures().contains("CHARGING_MANAGEMENT"));

        // Verify interactions
        verify(vehicleService, times(1)).getVehicleById(vehicleId);
        verify(featureAvailabilityService, times(1)).buildAvailableFeatures(any(Vehicle.class));
    }

    @Test
    void testGetAvailableFeatures_ForICEVehicle_ShouldReturnICEFeatures() {
        // Given
        Long vehicleId = 2L;
        List<String> iceFeatures = Arrays.asList(
            "FUEL_CONSUMPTION", 
            "FUEL_STATION_DISCOVERY", 
            "ENGINE_DIAGNOSTICS", 
            "MAINTENANCE_SCHEDULING"
        );
        AvailableFeaturesDTO iceFeaturesDTO = AvailableFeaturesDTO.builder()
                .features(iceFeatures)
                .batteryTrackingAvailable(false)
                .chargingManagementAvailable(false)
                .energyAnalyticsAvailable(false)
                .fuelConsumptionAvailable(true)
                .fuelStationDiscoveryAvailable(true)
                .engineDiagnosticsAvailable(true)
                .build();

        when(vehicleService.getVehicleById(vehicleId)).thenReturn(iceVehicleResponse);
        when(featureAvailabilityService.buildAvailableFeatures(any(Vehicle.class))).thenReturn(iceFeaturesDTO);

        // When
        ResponseEntity<AvailableFeaturesDTO> response = vehicleController.getAvailableFeatures(vehicleId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        AvailableFeaturesDTO features = response.getBody();
        assertFalse(features.isBatteryTrackingAvailable());
        assertFalse(features.isChargingManagementAvailable());
        assertFalse(features.isEnergyAnalyticsAvailable());
        assertTrue(features.isFuelConsumptionAvailable());
        assertTrue(features.isFuelStationDiscoveryAvailable());
        assertTrue(features.isEngineDiagnosticsAvailable());
        
        assertEquals(4, features.getFeatures().size());
        assertTrue(features.getFeatures().contains("FUEL_CONSUMPTION"));
        assertTrue(features.getFeatures().contains("ENGINE_DIAGNOSTICS"));

        verify(vehicleService, times(1)).getVehicleById(vehicleId);
        verify(featureAvailabilityService, times(1)).buildAvailableFeatures(any(Vehicle.class));
    }

    @Test
    void testGetAvailableFeatures_ForHybridVehicle_ShouldReturnAllFeatures() {
        // Given
        Long vehicleId = 3L;
        List<String> hybridFeatures = Arrays.asList(
            "BATTERY_TRACKING", 
            "CHARGING_MANAGEMENT", 
            "ENERGY_ANALYTICS", 
            "RANGE_PREDICTION", 
            "CHARGING_STATION_DISCOVERY",
            "FUEL_CONSUMPTION", 
            "FUEL_STATION_DISCOVERY", 
            "ENGINE_DIAGNOSTICS", 
            "MAINTENANCE_SCHEDULING",
            "HYBRID_MODE_OPTIMIZATION"
        );
        AvailableFeaturesDTO hybridFeaturesDTO = AvailableFeaturesDTO.builder()
                .features(hybridFeatures)
                .batteryTrackingAvailable(true)
                .chargingManagementAvailable(true)
                .energyAnalyticsAvailable(true)
                .fuelConsumptionAvailable(true)
                .fuelStationDiscoveryAvailable(true)
                .engineDiagnosticsAvailable(true)
                .build();

        when(vehicleService.getVehicleById(vehicleId)).thenReturn(hybridVehicleResponse);
        when(featureAvailabilityService.buildAvailableFeatures(any(Vehicle.class))).thenReturn(hybridFeaturesDTO);

        // When
        ResponseEntity<AvailableFeaturesDTO> response = vehicleController.getAvailableFeatures(vehicleId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        AvailableFeaturesDTO features = response.getBody();
        assertTrue(features.isBatteryTrackingAvailable());
        assertTrue(features.isChargingManagementAvailable());
        assertTrue(features.isEnergyAnalyticsAvailable());
        assertTrue(features.isFuelConsumptionAvailable());
        assertTrue(features.isFuelStationDiscoveryAvailable());
        assertTrue(features.isEngineDiagnosticsAvailable());
        
        assertEquals(10, features.getFeatures().size());
        assertTrue(features.getFeatures().contains("BATTERY_TRACKING"));
        assertTrue(features.getFeatures().contains("FUEL_CONSUMPTION"));
        assertTrue(features.getFeatures().contains("HYBRID_MODE_OPTIMIZATION"));

        verify(vehicleService, times(1)).getVehicleById(vehicleId);
        verify(featureAvailabilityService, times(1)).buildAvailableFeatures(any(Vehicle.class));
    }

    @Test
    void testGetAvailableFeatures_VerifiesFuelTypeIsPassedCorrectly() {
        // Given
        Long vehicleId = 1L;
        AvailableFeaturesDTO featuresDTO = AvailableFeaturesDTO.builder()
                .features(Arrays.asList("BATTERY_TRACKING"))
                .build();

        when(vehicleService.getVehicleById(vehicleId)).thenReturn(evVehicleResponse);
        when(featureAvailabilityService.buildAvailableFeatures(any(Vehicle.class))).thenReturn(featuresDTO);

        // When
        vehicleController.getAvailableFeatures(vehicleId);

        // Then - Capture the Vehicle argument passed to featureAvailabilityService
        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(featureAvailabilityService).buildAvailableFeatures(vehicleCaptor.capture());
        
        Vehicle capturedVehicle = vehicleCaptor.getValue();
        assertNotNull(capturedVehicle);
        assertEquals(FuelType.EV, capturedVehicle.getFuelType());
    }

    @Test
    void testGetAvailableFeatures_ValidatesServiceInteractions() {
        // Given
        Long vehicleId = 1L;
        AvailableFeaturesDTO featuresDTO = AvailableFeaturesDTO.builder()
                .features(Arrays.asList("BATTERY_TRACKING"))
                .build();

        when(vehicleService.getVehicleById(vehicleId)).thenReturn(evVehicleResponse);
        when(featureAvailabilityService.buildAvailableFeatures(any(Vehicle.class))).thenReturn(featuresDTO);

        // When
        ResponseEntity<AvailableFeaturesDTO> response = vehicleController.getAvailableFeatures(vehicleId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify that vehicle service was called first
        verify(vehicleService, times(1)).getVehicleById(vehicleId);
        // Then feature availability service was called
        verify(featureAvailabilityService, times(1)).buildAvailableFeatures(any(Vehicle.class));
    }
}
