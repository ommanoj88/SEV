package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.EnergyConsumptionResponse;
import com.evfleet.analytics.model.EnergyConsumptionAnalytics;
import com.evfleet.analytics.repository.EnergyConsumptionAnalyticsRepository;
import com.evfleet.charging.model.ChargingSession;
import com.evfleet.charging.repository.ChargingSessionRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EnergyAnalyticsService
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class EnergyAnalyticsServiceTest {

    @Mock
    private EnergyConsumptionAnalyticsRepository energyAnalyticsRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ChargingSessionRepository chargingSessionRepository;

    @InjectMocks
    private EnergyAnalyticsService energyAnalyticsService;

    private Vehicle testVehicle;
    private List<Trip> testTrips;
    private List<ChargingSession> testChargingSessions;

    @BeforeEach
    void setUp() {
        // Setup test vehicle
        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setCompanyId(1L);
        testVehicle.setMake("Tesla");
        testVehicle.setModel("Model 3");
        testVehicle.setFuelType(FuelType.EV);

        // Setup test trips
        Trip trip1 = new Trip();
        trip1.setId(1L);
        trip1.setVehicleId(1L);
        trip1.setDistance(100.0);
        trip1.setEnergyConsumed(new BigDecimal("15.0"));

        Trip trip2 = new Trip();
        trip2.setId(2L);
        trip2.setVehicleId(1L);
        trip2.setDistance(50.0);
        trip2.setEnergyConsumed(new BigDecimal("8.0"));

        testTrips = List.of(trip1, trip2);

        // Setup test charging sessions
        ChargingSession session1 = new ChargingSession();
        session1.setId(1L);
        session1.setVehicleId(1L);
        session1.setEnergyConsumed(new BigDecimal("20.0"));
        session1.setCost(new BigDecimal("5.00"));

        ChargingSession session2 = new ChargingSession();
        session2.setId(2L);
        session2.setVehicleId(1L);
        session2.setEnergyConsumed(new BigDecimal("15.0"));
        session2.setCost(new BigDecimal("3.75"));

        testChargingSessions = List.of(session1, session2);
    }

    @Test
    void testAggregateDailyEnergyAnalytics_Success() {
        // Arrange
        LocalDate date = LocalDate.now();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(energyAnalyticsRepository.findByVehicleIdAndAnalysisDate(1L, date))
                .thenReturn(Optional.empty());
        when(tripRepository.findByVehicleIdAndStartTimeBetween(1L, startOfDay, endOfDay))
                .thenReturn(testTrips);
        when(chargingSessionRepository.findByVehicleIdAndStartTimeBetween(1L, startOfDay, endOfDay))
                .thenReturn(testChargingSessions);
        when(energyAnalyticsRepository.save(any(EnergyConsumptionAnalytics.class)))
                .thenAnswer(invocation -> {
                    EnergyConsumptionAnalytics analytics = invocation.getArgument(0);
                    analytics.setId(1L);
                    return analytics;
                });

        // Act
        EnergyConsumptionResponse response = energyAnalyticsService.aggregateDailyEnergyAnalytics(1L, date);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getVehicleId());
        assertEquals("Tesla Model 3", response.getVehicleName());
        assertEquals(2, response.getChargingSessions());

        verify(energyAnalyticsRepository, times(1)).save(any(EnergyConsumptionAnalytics.class));
    }

    @Test
    void testAggregateDailyEnergyAnalytics_NonElectricVehicle() {
        // Arrange
        testVehicle.setFuelType(FuelType.ICE);
        LocalDate date = LocalDate.now();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act
        EnergyConsumptionResponse response = energyAnalyticsService.aggregateDailyEnergyAnalytics(1L, date);

        // Assert
        assertNull(response);
        verify(energyAnalyticsRepository, never()).save(any(EnergyConsumptionAnalytics.class));
    }

    @Test
    void testGetEnergyConsumption_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        EnergyConsumptionAnalytics analytics1 = EnergyConsumptionAnalytics.builder()
                .id(1L)
                .companyId(1L)
                .vehicleId(1L)
                .analysisDate(startDate)
                .totalEnergyConsumed(new BigDecimal("20.0"))
                .totalDistance(new BigDecimal("100.0"))
                .totalChargingSessions(2)
                .averageEfficiency(new BigDecimal("20.0"))
                .totalChargingCost(new BigDecimal("5.00"))
                .build();

        EnergyConsumptionAnalytics analytics2 = EnergyConsumptionAnalytics.builder()
                .id(2L)
                .companyId(1L)
                .vehicleId(1L)
                .analysisDate(endDate)
                .totalEnergyConsumed(new BigDecimal("25.0"))
                .totalDistance(new BigDecimal("120.0"))
                .totalChargingSessions(3)
                .averageEfficiency(new BigDecimal("20.8"))
                .totalChargingCost(new BigDecimal("6.25"))
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(energyAnalyticsRepository.findByVehicleIdAndAnalysisDateBetween(1L, startDate, endDate))
                .thenReturn(List.of(analytics1, analytics2));

        // Act
        List<EnergyConsumptionResponse> responses = energyAnalyticsService.getEnergyConsumption(1L, startDate, endDate);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(new BigDecimal("20.0"), responses.get(0).getEnergyConsumed());
        assertEquals(new BigDecimal("25.0"), responses.get(1).getEnergyConsumed());

        verify(energyAnalyticsRepository, times(1)).findByVehicleIdAndAnalysisDateBetween(1L, startDate, endDate);
    }

    @Test
    void testGetEnergyConsumptionForDate_ExistingData() {
        // Arrange
        LocalDate date = LocalDate.now();
        EnergyConsumptionAnalytics analytics = EnergyConsumptionAnalytics.builder()
                .id(1L)
                .companyId(1L)
                .vehicleId(1L)
                .analysisDate(date)
                .totalEnergyConsumed(new BigDecimal("20.0"))
                .totalDistance(new BigDecimal("100.0"))
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(energyAnalyticsRepository.findByVehicleIdAndAnalysisDate(1L, date))
                .thenReturn(Optional.of(analytics));

        // Act
        EnergyConsumptionResponse response = energyAnalyticsService.getEnergyConsumptionForDate(1L, date);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("20.0"), response.getEnergyConsumed());

        verify(energyAnalyticsRepository, times(1)).findByVehicleIdAndAnalysisDate(1L, date);
    }

    @Test
    void testCompareVehicleEfficiency_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId(2L);
        vehicle2.setCompanyId(1L);
        vehicle2.setMake("Nissan");
        vehicle2.setModel("Leaf");
        vehicle2.setFuelType(FuelType.EV);

        when(vehicleRepository.findByCompanyId(1L)).thenReturn(List.of(testVehicle, vehicle2));

        EnergyConsumptionAnalytics analytics1 = EnergyConsumptionAnalytics.builder()
                .vehicleId(1L)
                .totalEnergyConsumed(new BigDecimal("20.0"))
                .totalDistance(new BigDecimal("100.0"))
                .totalChargingSessions(2)
                .totalChargingCost(new BigDecimal("5.00"))
                .build();

        when(energyAnalyticsRepository.findByVehicleIdAndAnalysisDateBetween(1L, startDate, endDate))
                .thenReturn(List.of(analytics1));
        when(energyAnalyticsRepository.findByVehicleIdAndAnalysisDateBetween(2L, startDate, endDate))
                .thenReturn(List.of());

        // Act
        List<EnergyConsumptionResponse> comparison = energyAnalyticsService.compareVehicleEfficiency(1L, startDate, endDate);

        // Assert
        assertNotNull(comparison);
        assertEquals(1, comparison.size()); // Only vehicle with data
        assertEquals(1L, comparison.get(0).getVehicleId());

        verify(vehicleRepository, times(1)).findByCompanyId(1L);
    }
}
