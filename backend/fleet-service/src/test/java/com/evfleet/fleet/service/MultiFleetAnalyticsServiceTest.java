package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.TripCostAnalysisDTO;
import com.evfleet.fleet.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MultiFleetAnalyticsService
 * Tests multi-fuel trip analytics and cost calculations
 * 
 * @since 2.0.0 (Multi-fuel support - PR 7)
 */
@ExtendWith(MockitoExtension.class)
class MultiFleetAnalyticsServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private EVCostCalculator evCostCalculator;

    @Mock
    private ICECostCalculator iceCostCalculator;

    @InjectMocks
    private MultiFleetAnalyticsService analyticsService;

    private Vehicle evVehicle;
    private Vehicle iceVehicle;
    private Vehicle hybridVehicle;
    private Trip evTrip;
    private Trip iceTrip;
    private Trip hybridTrip;

    @BeforeEach
    void setUp() {
        // Create EV vehicle and trip
        evVehicle = createVehicle(1L, "EV001", FuelType.EV);
        evTrip = createTrip(1L, 1L, 100.0, 15.0, 95.0, 75.0);

        // Create ICE vehicle and trip
        iceVehicle = createVehicle(2L, "ICE001", FuelType.ICE);
        iceVehicle.setEngineType("DIESEL");
        iceTrip = createTrip(2L, 2L, 100.0, null, null, null);

        // Create HYBRID vehicle and trip
        hybridVehicle = createVehicle(3L, "HYB001", FuelType.HYBRID);
        hybridTrip = createTrip(3L, 3L, 100.0, 10.0, 90.0, 78.0);
    }

    @Test
    void testGetTripCostAnalysis_forEVTrip() {
        // Given
        when(tripRepository.findById(1L)).thenReturn(Optional.of(evTrip));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(evVehicle));
        when(evCostCalculator.calculateEnergyCost(evTrip, evVehicle)).thenReturn(120.0);
        when(evCostCalculator.calculateEnergyEfficiency(evTrip)).thenReturn(15.0);
        when(evCostCalculator.calculateCarbonFootprint(evTrip)).thenReturn(12.3);
        when(evCostCalculator.calculateCostPerKm(evTrip, evVehicle)).thenReturn(1.20);

        // When
        TripCostAnalysisDTO result = analyticsService.getTripCostAnalysis(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getTripId());
        assertEquals(1L, result.getVehicleId());
        assertEquals("EV001", result.getVehicleNumber());
        assertEquals(FuelType.EV, result.getFuelType());
        assertEquals(100.0, result.getDistance());
        assertEquals(120.0, result.getEnergyCost());
        assertEquals(120.0, result.getTotalCost());
        assertEquals(1.20, result.getCostPerKm());
        assertEquals(15.0, result.getEnergyConsumed());
        assertEquals(15.0, result.getEnergyEfficiency());
        assertEquals(12.3, result.getCarbonFootprint());
        assertEquals(20.0, result.getBatteryConsumed()); // 95 - 75
        assertNull(result.getFuelCost());
        assertNull(result.getMileage());
    }

    @Test
    void testGetTripCostAnalysis_forICETrip() {
        // Given
        when(tripRepository.findById(2L)).thenReturn(Optional.of(iceTrip));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(iceVehicle));
        when(iceCostCalculator.calculateFuelCost(iceTrip, iceVehicle)).thenReturn(760.0);
        when(iceCostCalculator.calculateFuelEfficiency(iceTrip, iceVehicle)).thenReturn(8.0);
        when(iceCostCalculator.calculateMileage(iceTrip, iceVehicle)).thenReturn(12.5);
        when(iceCostCalculator.calculateCarbonFootprint(iceTrip, iceVehicle)).thenReturn(21.44);
        when(iceCostCalculator.calculateCostPerKm(iceTrip, iceVehicle)).thenReturn(7.60);

        // When
        TripCostAnalysisDTO result = analyticsService.getTripCostAnalysis(2L);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getTripId());
        assertEquals(2L, result.getVehicleId());
        assertEquals("ICE001", result.getVehicleNumber());
        assertEquals(FuelType.ICE, result.getFuelType());
        assertEquals(100.0, result.getDistance());
        assertEquals(760.0, result.getFuelCost());
        assertEquals(760.0, result.getTotalCost());
        assertEquals(7.60, result.getCostPerKm());
        assertEquals(8.0, result.getFuelEfficiency());
        assertEquals(12.5, result.getMileage());
        assertEquals(21.44, result.getCarbonFootprint());
        assertNull(result.getEnergyCost());
        assertNull(result.getEnergyEfficiency());
    }

    @Test
    void testGetTripCostAnalysis_forHybridTrip() {
        // Given
        when(tripRepository.findById(3L)).thenReturn(Optional.of(hybridTrip));
        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(hybridVehicle));
        when(evCostCalculator.calculateEnergyCost(hybridTrip, hybridVehicle)).thenReturn(80.0);
        when(iceCostCalculator.calculateFuelCost(hybridTrip, hybridVehicle)).thenReturn(380.0);
        when(evCostCalculator.calculateCarbonFootprint(hybridTrip)).thenReturn(8.2);
        when(iceCostCalculator.calculateCarbonFootprint(hybridTrip, hybridVehicle)).thenReturn(10.72);

        // When
        TripCostAnalysisDTO result = analyticsService.getTripCostAnalysis(3L);

        // Then
        assertNotNull(result);
        assertEquals(3L, result.getTripId());
        assertEquals(FuelType.HYBRID, result.getFuelType());
        assertEquals(80.0, result.getEnergyCost());
        assertEquals(380.0, result.getFuelCost());
        assertEquals(460.0, result.getTotalCost()); // 80 + 380
        assertEquals(4.60, result.getCostPerKm()); // 460 / 100
        assertEquals(18.92, result.getCarbonFootprint()); // 8.2 + 10.72
    }

    @Test
    void testGetTripCostAnalysis_tripNotFound() {
        // Given
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                    () -> analyticsService.getTripCostAnalysis(999L));
    }

    @Test
    void testGetTripCostAnalysis_vehicleNotFound() {
        // Given
        when(tripRepository.findById(1L)).thenReturn(Optional.of(evTrip));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                    () -> analyticsService.getTripCostAnalysis(1L));
    }

    @Test
    void testGetTripCostAnalysisByVehicle_withTimeRange() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(evVehicle));
        when(tripRepository.findTripsByVehicleAndTimeRange(1L, startTime, endTime))
                .thenReturn(Collections.singletonList(evTrip));
        when(evCostCalculator.calculateEnergyCost(any(), any())).thenReturn(120.0);
        when(evCostCalculator.calculateEnergyEfficiency(any())).thenReturn(15.0);
        when(evCostCalculator.calculateCarbonFootprint(any())).thenReturn(12.3);
        when(evCostCalculator.calculateCostPerKm(any(), any())).thenReturn(1.20);

        // When
        List<TripCostAnalysisDTO> results = analyticsService.getTripCostAnalysisByVehicle(
                1L, startTime, endTime);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getTripId());
        assertEquals(FuelType.EV, results.get(0).getFuelType());
    }

    @Test
    void testGetTripCostAnalysisByVehicle_withoutTimeRange() {
        // Given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(evVehicle));
        when(tripRepository.findByVehicleId(1L, null))
                .thenReturn(Collections.singletonList(evTrip));
        when(evCostCalculator.calculateEnergyCost(any(), any())).thenReturn(120.0);
        when(evCostCalculator.calculateEnergyEfficiency(any())).thenReturn(15.0);
        when(evCostCalculator.calculateCarbonFootprint(any())).thenReturn(12.3);
        when(evCostCalculator.calculateCostPerKm(any(), any())).thenReturn(1.20);

        // When
        List<TripCostAnalysisDTO> results = analyticsService.getTripCostAnalysisByVehicle(
                1L, null, null);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void testGetCompanyCostSummary() {
        // Given
        Long companyId = 1L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();

        List<Trip> trips = Arrays.asList(evTrip, iceTrip, hybridTrip);
        List<Vehicle> vehicles = Arrays.asList(evVehicle, iceVehicle, hybridVehicle);

        when(tripRepository.findTripsByCompanyAndTimeRange(companyId, startTime, endTime))
                .thenReturn(trips);
        when(vehicleRepository.findAllById(anySet())).thenReturn(vehicles);

        // Mock EV calculations
        when(evCostCalculator.calculateEnergyCost(evTrip, evVehicle)).thenReturn(120.0);
        when(evCostCalculator.calculateCarbonFootprint(evTrip)).thenReturn(12.3);

        // Mock ICE calculations
        when(iceCostCalculator.calculateFuelCost(iceTrip, iceVehicle)).thenReturn(760.0);
        when(iceCostCalculator.calculateCarbonFootprint(iceTrip, iceVehicle)).thenReturn(21.44);

        // Mock Hybrid calculations
        when(evCostCalculator.calculateEnergyCost(hybridTrip, hybridVehicle)).thenReturn(80.0);
        when(iceCostCalculator.calculateFuelCost(hybridTrip, hybridVehicle)).thenReturn(380.0);
        when(evCostCalculator.calculateCarbonFootprint(hybridTrip)).thenReturn(8.2);
        when(iceCostCalculator.calculateCarbonFootprint(hybridTrip, hybridVehicle)).thenReturn(10.72);

        // When
        Map<String, Object> summary = analyticsService.getCompanyCostSummary(companyId, startTime, endTime);

        // Then
        assertNotNull(summary);
        assertEquals(companyId, summary.get("companyId"));
        assertEquals(3, summary.get("totalTrips"));
        assertEquals(1, summary.get("evTripCount"));
        assertEquals(1, summary.get("iceTripCount"));
        assertEquals(1, summary.get("hybridTripCount"));
        assertEquals(300.0, summary.get("totalDistance")); // 100 + 100 + 100
        assertEquals(120.0, summary.get("totalEVCost"));
        assertEquals(760.0, summary.get("totalICECost"));
        assertEquals(460.0, summary.get("totalHybridCost")); // 80 + 380
        assertEquals(1340.0, summary.get("totalCost")); // 120 + 760 + 460
        assertEquals(4.47, summary.get("avgCostPerKm")); // 1340 / 300
        assertEquals(52.66, summary.get("totalCarbonFootprint")); // 12.3 + 21.44 + 8.2 + 10.72
    }

    @Test
    void testGetCompanyCostSummary_noTrips() {
        // Given
        Long companyId = 1L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();

        when(tripRepository.findTripsByCompanyAndTimeRange(companyId, startTime, endTime))
                .thenReturn(Collections.emptyList());

        // When
        Map<String, Object> summary = analyticsService.getCompanyCostSummary(companyId, startTime, endTime);

        // Then
        assertNotNull(summary);
        assertTrue(summary.isEmpty());
    }

    @Test
    void testCompareFuelTypeCosts() {
        // Given
        Long companyId = 1L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();

        List<Trip> trips = Arrays.asList(evTrip, iceTrip);
        List<Vehicle> vehicles = Arrays.asList(evVehicle, iceVehicle);

        when(tripRepository.findTripsByCompanyAndTimeRange(companyId, startTime, endTime))
                .thenReturn(trips);
        when(vehicleRepository.findAllById(anySet())).thenReturn(vehicles);

        when(evCostCalculator.calculateEnergyCost(evTrip, evVehicle)).thenReturn(120.0);
        when(evCostCalculator.calculateCarbonFootprint(evTrip)).thenReturn(12.3);
        when(iceCostCalculator.calculateFuelCost(iceTrip, iceVehicle)).thenReturn(760.0);
        when(iceCostCalculator.calculateCarbonFootprint(iceTrip, iceVehicle)).thenReturn(21.44);

        // When
        Map<String, Object> comparison = analyticsService.compareFuelTypeCosts(companyId, startTime, endTime);

        // Then
        assertNotNull(comparison);
        assertEquals(companyId, comparison.get("companyId"));
        assertEquals(120.0, comparison.get("evTotalCost"));
        assertEquals(760.0, comparison.get("iceTotalCost"));
        assertEquals(1, comparison.get("evTripCount"));
        assertEquals(1, comparison.get("iceTripCount"));
        assertEquals(120.0, comparison.get("avgEVCostPerTrip")); // 120 / 1
        assertEquals(760.0, comparison.get("avgICECostPerTrip")); // 760 / 1
        assertEquals(84.21, comparison.get("evCostSavingsPercent")); // ((760 - 120) / 760) * 100
    }

    @Test
    void testCompareFuelTypeCosts_noTrips() {
        // Given
        Long companyId = 1L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();

        when(tripRepository.findTripsByCompanyAndTimeRange(companyId, startTime, endTime))
                .thenReturn(Collections.emptyList());

        // When
        Map<String, Object> comparison = analyticsService.compareFuelTypeCosts(companyId, startTime, endTime);

        // Then
        assertNotNull(comparison);
        assertTrue(comparison.isEmpty());
    }

    // Helper methods

    private Vehicle createVehicle(Long id, String vehicleNumber, FuelType fuelType) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setVehicleNumber(vehicleNumber);
        vehicle.setFuelType(fuelType);
        vehicle.setCompanyId(1L);
        return vehicle;
    }

    private Trip createTrip(Long id, Long vehicleId, Double distance, 
                           Double energyConsumed, Double startSoc, Double endSoc) {
        Trip trip = new Trip();
        trip.setId(id);
        trip.setVehicleId(vehicleId);
        trip.setDistance(distance);
        trip.setEnergyConsumed(energyConsumed);
        trip.setStartBatterySoc(startSoc);
        trip.setEndBatterySoc(endSoc);
        trip.setStartTime(LocalDateTime.now().minusHours(2));
        trip.setEndTime(LocalDateTime.now());
        trip.setStatus(Trip.TripStatus.COMPLETED);
        trip.setDurationMinutes(120);
        trip.setEfficiencyScore(85.0);
        trip.setAverageSpeed(50.0);
        trip.setHarshAccelerationCount(2);
        trip.setHarshBrakingCount(1);
        trip.setOverspeedingCount(0);
        trip.setIdleTimeMinutes(10);
        trip.setCompanyId(1L);
        return trip;
    }
}
