package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.AvailableFeaturesDTO;
import com.evfleet.fleet.dto.VehicleResponse;
import com.evfleet.fleet.event.EventPublisher;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.fleet.service.impl.VehicleServiceImpl;
import com.evfleet.fleet.validation.FuelTypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VehicleService - PR 4: Multi-fuel Query Methods
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private FuelTypeValidator fuelTypeValidator;

    @Mock
    private FeatureAvailabilityService featureAvailabilityService;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle evVehicle;
    private Vehicle iceVehicle;
    private Vehicle hybridVehicle;

    private static final Long COMPANY_ID = 1L;

    @BeforeEach
    void setUp() {
        // Create EV vehicle
        evVehicle = new Vehicle();
        evVehicle.setId(1L);
        evVehicle.setCompanyId(COMPANY_ID);
        evVehicle.setVehicleNumber("EV001");
        evVehicle.setFuelType(FuelType.EV);
        evVehicle.setType(Vehicle.VehicleType.LCV);
        evVehicle.setMake("Tesla");
        evVehicle.setModel("Model 3");
        evVehicle.setYear(2024);
        evVehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        evVehicle.setBatteryCapacity(75.0);
        evVehicle.setCurrentBatterySoc(15.0);
        evVehicle.setCreatedAt(LocalDateTime.now());
        evVehicle.setUpdatedAt(LocalDateTime.now());

        // Create ICE vehicle
        iceVehicle = new Vehicle();
        iceVehicle.setId(2L);
        iceVehicle.setCompanyId(COMPANY_ID);
        iceVehicle.setVehicleNumber("ICE001");
        iceVehicle.setFuelType(FuelType.ICE);
        iceVehicle.setType(Vehicle.VehicleType.LCV);
        iceVehicle.setMake("Ford");
        iceVehicle.setModel("Transit");
        iceVehicle.setYear(2024);
        iceVehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        iceVehicle.setFuelTankCapacity(60.0);
        iceVehicle.setFuelLevel(10.0);
        iceVehicle.setCreatedAt(LocalDateTime.now());
        iceVehicle.setUpdatedAt(LocalDateTime.now());

        // Create Hybrid vehicle
        hybridVehicle = new Vehicle();
        hybridVehicle.setId(3L);
        hybridVehicle.setCompanyId(COMPANY_ID);
        hybridVehicle.setVehicleNumber("HYB001");
        hybridVehicle.setFuelType(FuelType.HYBRID);
        hybridVehicle.setType(Vehicle.VehicleType.LCV);
        hybridVehicle.setMake("Toyota");
        hybridVehicle.setModel("Prius");
        hybridVehicle.setYear(2024);
        hybridVehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        hybridVehicle.setBatteryCapacity(50.0);
        hybridVehicle.setCurrentBatterySoc(30.0);
        hybridVehicle.setFuelTankCapacity(45.0);
        hybridVehicle.setFuelLevel(25.0);
        hybridVehicle.setCreatedAt(LocalDateTime.now());
        hybridVehicle.setUpdatedAt(LocalDateTime.now());

        // PR 5: Setup lenient mock for FeatureAvailabilityService
        // Mock to return a valid AvailableFeaturesDTO for any vehicle
        lenient().when(featureAvailabilityService.buildAvailableFeatures(any(Vehicle.class)))
                .thenAnswer(invocation -> {
                    Vehicle v = invocation.getArgument(0);
                    AvailableFeaturesDTO features = new AvailableFeaturesDTO();
                    if (v.getFuelType() == FuelType.EV) {
                        features.setBatteryTrackingAvailable(true);
                        features.setChargingManagementAvailable(true);
                        features.setFuelConsumptionAvailable(false);
                    } else if (v.getFuelType() == FuelType.ICE) {
                        features.setBatteryTrackingAvailable(false);
                        features.setChargingManagementAvailable(false);
                        features.setFuelConsumptionAvailable(true);
                    } else if (v.getFuelType() == FuelType.HYBRID) {
                        features.setBatteryTrackingAvailable(true);
                        features.setChargingManagementAvailable(true);
                        features.setFuelConsumptionAvailable(true);
                    }
                    return features;
                });
    }

    @Test
    void testGetVehiclesByFuelType_EV() {
        // Given
        List<Vehicle> evVehicles = new ArrayList<>();
        evVehicles.add(evVehicle);
        when(vehicleRepository.findByFuelType(FuelType.EV)).thenReturn(evVehicles);

        // When
        List<VehicleResponse> result = vehicleService.getVehiclesByFuelType(FuelType.EV);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EV001", result.get(0).getVehicleNumber());
        assertEquals(FuelType.EV, result.get(0).getFuelType());
        verify(vehicleRepository, times(1)).findByFuelType(FuelType.EV);
    }

    @Test
    void testGetVehiclesByFuelType_ICE() {
        // Given
        List<Vehicle> iceVehicles = new ArrayList<>();
        iceVehicles.add(iceVehicle);
        when(vehicleRepository.findByFuelType(FuelType.ICE)).thenReturn(iceVehicles);

        // When
        List<VehicleResponse> result = vehicleService.getVehiclesByFuelType(FuelType.ICE);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ICE001", result.get(0).getVehicleNumber());
        assertEquals(FuelType.ICE, result.get(0).getFuelType());
        verify(vehicleRepository, times(1)).findByFuelType(FuelType.ICE);
    }

    @Test
    void testGetVehiclesByFuelType_NoResults() {
        // Given
        when(vehicleRepository.findByFuelType(FuelType.HYBRID)).thenReturn(new ArrayList<>());

        // When
        List<VehicleResponse> result = vehicleService.getVehiclesByFuelType(FuelType.HYBRID);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(vehicleRepository, times(1)).findByFuelType(FuelType.HYBRID);
    }

    @Test
    void testGetVehiclesByCompanyAndFuelType() {
        // Given
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(evVehicle);
        when(vehicleRepository.findByCompanyIdAndFuelType(COMPANY_ID, FuelType.EV)).thenReturn(vehicles);

        // When
        List<VehicleResponse> result = vehicleService.getVehiclesByCompanyAndFuelType(COMPANY_ID, FuelType.EV);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(COMPANY_ID, result.get(0).getCompanyId());
        assertEquals(FuelType.EV, result.get(0).getFuelType());
        verify(vehicleRepository, times(1)).findByCompanyIdAndFuelType(COMPANY_ID, FuelType.EV);
    }

    @Test
    void testGetFleetComposition() {
        // Given
        List<Object[]> compositionData = new ArrayList<>();
        compositionData.add(new Object[]{FuelType.EV, 2L});
        compositionData.add(new Object[]{FuelType.ICE, 1L});
        compositionData.add(new Object[]{FuelType.HYBRID, 1L});
        
        when(vehicleRepository.getFleetCompositionByCompany(COMPANY_ID)).thenReturn(compositionData);

        // When
        Map<String, Object> result = vehicleService.getFleetComposition(COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(4L, result.get("totalVehicles"));
        
        @SuppressWarnings("unchecked")
        Map<String, Long> counts = (Map<String, Long>) result.get("counts");
        assertEquals(2L, counts.get("EV"));
        assertEquals(1L, counts.get("ICE"));
        assertEquals(1L, counts.get("HYBRID"));
        
        @SuppressWarnings("unchecked")
        Map<String, Double> percentages = (Map<String, Double>) result.get("percentages");
        assertEquals(50.0, percentages.get("EV"), 0.01);
        assertEquals(25.0, percentages.get("ICE"), 0.01);
        assertEquals(25.0, percentages.get("HYBRID"), 0.01);
        
        verify(vehicleRepository, times(1)).getFleetCompositionByCompany(COMPANY_ID);
    }

    @Test
    void testGetFleetComposition_EmptyFleet() {
        // Given
        when(vehicleRepository.getFleetCompositionByCompany(COMPANY_ID)).thenReturn(new ArrayList<>());

        // When
        Map<String, Object> result = vehicleService.getFleetComposition(COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.get("totalVehicles"));
        
        @SuppressWarnings("unchecked")
        Map<String, Long> counts = (Map<String, Long>) result.get("counts");
        assertTrue(counts.isEmpty());
        
        @SuppressWarnings("unchecked")
        Map<String, Double> percentages = (Map<String, Double>) result.get("percentages");
        assertTrue(percentages.isEmpty());
        
        verify(vehicleRepository, times(1)).getFleetCompositionByCompany(COMPANY_ID);
    }

    @Test
    void testGetFleetComposition_SingleFuelType() {
        // Given - Only EV vehicles
        List<Object[]> compositionData = new ArrayList<>();
        compositionData.add(new Object[]{FuelType.EV, 5L});
        
        when(vehicleRepository.getFleetCompositionByCompany(COMPANY_ID)).thenReturn(compositionData);

        // When
        Map<String, Object> result = vehicleService.getFleetComposition(COMPANY_ID);

        // Then
        assertNotNull(result);
        assertEquals(5L, result.get("totalVehicles"));
        
        @SuppressWarnings("unchecked")
        Map<String, Long> counts = (Map<String, Long>) result.get("counts");
        assertEquals(5L, counts.get("EV"));
        assertEquals(1, counts.size());
        
        @SuppressWarnings("unchecked")
        Map<String, Double> percentages = (Map<String, Double>) result.get("percentages");
        assertEquals(100.0, percentages.get("EV"), 0.01);
        assertEquals(1, percentages.size());
    }

    @Test
    void testGetLowBatteryVehicles() {
        // Given - EV with 15% battery
        List<Vehicle> lowBatteryVehicles = new ArrayList<>();
        lowBatteryVehicles.add(evVehicle);
        when(vehicleRepository.findLowBatteryVehicles(COMPANY_ID, 20.0)).thenReturn(lowBatteryVehicles);

        // When
        List<VehicleResponse> result = vehicleService.getLowBatteryVehicles(COMPANY_ID, 20.0);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EV001", result.get(0).getVehicleNumber());
        assertTrue(result.get(0).getCurrentBatterySoc() < 20.0);
        verify(vehicleRepository, times(1)).findLowBatteryVehicles(COMPANY_ID, 20.0);
    }

    @Test
    void testGetLowBatteryVehicles_NoResults() {
        // Given
        when(vehicleRepository.findLowBatteryVehicles(COMPANY_ID, 10.0)).thenReturn(new ArrayList<>());

        // When
        List<VehicleResponse> result = vehicleService.getLowBatteryVehicles(COMPANY_ID, 10.0);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(vehicleRepository, times(1)).findLowBatteryVehicles(COMPANY_ID, 10.0);
    }

    @Test
    void testGetLowBatteryVehicles_MultipleVehicles() {
        // Given - Multiple vehicles with low battery
        List<Vehicle> lowBatteryVehicles = new ArrayList<>();
        lowBatteryVehicles.add(evVehicle);
        lowBatteryVehicles.add(hybridVehicle);
        when(vehicleRepository.findLowBatteryVehicles(COMPANY_ID, 40.0)).thenReturn(lowBatteryVehicles);

        // When
        List<VehicleResponse> result = vehicleService.getLowBatteryVehicles(COMPANY_ID, 40.0);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(v -> 
            v.getFuelType() == FuelType.EV || v.getFuelType() == FuelType.HYBRID));
        verify(vehicleRepository, times(1)).findLowBatteryVehicles(COMPANY_ID, 40.0);
    }

    @Test
    void testGetLowFuelVehicles() {
        // Given - ICE with 10/60 = 16.67% fuel
        List<Vehicle> lowFuelVehicles = new ArrayList<>();
        lowFuelVehicles.add(iceVehicle);
        when(vehicleRepository.findLowFuelVehicles(COMPANY_ID, 20.0)).thenReturn(lowFuelVehicles);

        // When
        List<VehicleResponse> result = vehicleService.getLowFuelVehicles(COMPANY_ID, 20.0);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ICE001", result.get(0).getVehicleNumber());
        assertEquals(FuelType.ICE, result.get(0).getFuelType());
        verify(vehicleRepository, times(1)).findLowFuelVehicles(COMPANY_ID, 20.0);
    }

    @Test
    void testGetLowFuelVehicles_NoResults() {
        // Given
        when(vehicleRepository.findLowFuelVehicles(COMPANY_ID, 10.0)).thenReturn(new ArrayList<>());

        // When
        List<VehicleResponse> result = vehicleService.getLowFuelVehicles(COMPANY_ID, 10.0);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(vehicleRepository, times(1)).findLowFuelVehicles(COMPANY_ID, 10.0);
    }

    @Test
    void testGetLowFuelVehicles_MultipleVehicles() {
        // Given - ICE and Hybrid with low fuel
        List<Vehicle> lowFuelVehicles = new ArrayList<>();
        lowFuelVehicles.add(iceVehicle);
        lowFuelVehicles.add(hybridVehicle);
        when(vehicleRepository.findLowFuelVehicles(COMPANY_ID, 60.0)).thenReturn(lowFuelVehicles);

        // When
        List<VehicleResponse> result = vehicleService.getLowFuelVehicles(COMPANY_ID, 60.0);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(v -> 
            v.getFuelType() == FuelType.ICE || v.getFuelType() == FuelType.HYBRID));
        verify(vehicleRepository, times(1)).findLowFuelVehicles(COMPANY_ID, 60.0);
    }

    @Test
    void testMultiFuelQueryMethodsIntegration() {
        // This test verifies that all PR 4 methods are working together
        
        // Setup mocks for different fuel types
        when(vehicleRepository.findByFuelType(FuelType.EV))
            .thenReturn(List.of(evVehicle));
        when(vehicleRepository.findByFuelType(FuelType.ICE))
            .thenReturn(List.of(iceVehicle));
        when(vehicleRepository.findByFuelType(FuelType.HYBRID))
            .thenReturn(List.of(hybridVehicle));

        // Test each fuel type
        assertEquals(1, vehicleService.getVehiclesByFuelType(FuelType.EV).size());
        assertEquals(1, vehicleService.getVehiclesByFuelType(FuelType.ICE).size());
        assertEquals(1, vehicleService.getVehiclesByFuelType(FuelType.HYBRID).size());

        // Verify all repository methods were called
        verify(vehicleRepository, times(1)).findByFuelType(FuelType.EV);
        verify(vehicleRepository, times(1)).findByFuelType(FuelType.ICE);
        verify(vehicleRepository, times(1)).findByFuelType(FuelType.HYBRID);
    }
}
