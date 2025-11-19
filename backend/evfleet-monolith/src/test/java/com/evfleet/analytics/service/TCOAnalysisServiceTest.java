package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.TCOAnalysisResponse;
import com.evfleet.analytics.model.TCOAnalysis;
import com.evfleet.analytics.repository.TCOAnalysisRepository;
import com.evfleet.charging.model.ChargingSession;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.maintenance.model.MaintenanceRecord;
import com.evfleet.maintenance.repository.MaintenanceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TCOAnalysisService
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class TCOAnalysisServiceTest {

    @Mock
    private TCOAnalysisRepository tcoAnalysisRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ChargingSessionRepository chargingSessionRepository;

    @Mock
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @InjectMocks
    private TCOAnalysisService tcoAnalysisService;

    private Vehicle testVehicle;
    private List<Trip> testTrips;
    private List<ChargingSession> testChargingSessions;
    private List<MaintenanceRecord> testMaintenanceRecords;

    @BeforeEach
    void setUp() {
        // Setup test vehicle
        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setCompanyId(1L);
        testVehicle.setMake("Tesla");
        testVehicle.setModel("Model 3");
        testVehicle.setYear(2023);
        testVehicle.setVehicleNumber("TEST123");
        testVehicle.setFuelType(FuelType.EV);
        testVehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        testVehicle.setCreatedAt(LocalDateTime.now().minusYears(1));

        // Setup test trips
        Trip trip1 = new Trip();
        trip1.setId(1L);
        trip1.setVehicleId(1L);
        trip1.setDistance(100.0);
        trip1.setEnergyConsumed(new BigDecimal("15.0"));
        trip1.setStartTime(LocalDateTime.now().minusDays(1));
        trip1.setEndTime(LocalDateTime.now().minusDays(1).plusHours(2));

        Trip trip2 = new Trip();
        trip2.setId(2L);
        trip2.setVehicleId(1L);
        trip2.setDistance(50.0);
        trip2.setEnergyConsumed(new BigDecimal("8.0"));
        trip2.setStartTime(LocalDateTime.now().minusDays(2));
        trip2.setEndTime(LocalDateTime.now().minusDays(2).plusHours(1));

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

        // Setup test maintenance records
        MaintenanceRecord record1 = new MaintenanceRecord();
        record1.setId(1L);
        record1.setVehicleId(1L);
        record1.setCost(new BigDecimal("150.00"));

        MaintenanceRecord record2 = new MaintenanceRecord();
        record2.setId(2L);
        record2.setVehicleId(1L);
        record2.setCost(new BigDecimal("75.00"));

        testMaintenanceRecords = List.of(record1, record2);
    }

    @Test
    void testCalculateTCO_Success() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(tripRepository.findByVehicleId(1L)).thenReturn(testTrips);
        when(chargingSessionRepository.findByVehicleId(1L)).thenReturn(testChargingSessions);
        when(maintenanceRecordRepository.findByVehicleId(1L)).thenReturn(testMaintenanceRecords);
        when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.empty());
        when(tcoAnalysisRepository.save(any(TCOAnalysis.class))).thenAnswer(invocation -> {
            TCOAnalysis tco = invocation.getArgument(0);
            tco.setId(1L);
            return tco;
        });

        // Act
        TCOAnalysisResponse response = tcoAnalysisService.calculateTCO(1L, 5);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getVehicleId());
        assertEquals("Tesla Model 3", response.getVehicleName());
        assertEquals(FuelType.EV, response.getFuelType());
        assertNotNull(response.getTotalCost());
        assertNotNull(response.getEnergyCosts());
        assertNotNull(response.getMaintenanceCosts());

        // Verify interactions
        verify(vehicleRepository, times(1)).findById(1L);
        verify(tcoAnalysisRepository, times(1)).save(any(TCOAnalysis.class));
    }

    @Test
    void testGetTCOAnalysis_ExistingAnalysis() {
        // Arrange
        TCOAnalysis existingTCO = TCOAnalysis.builder()
                .id(1L)
                .companyId(1L)
                .vehicleId(1L)
                .analysisDate(LocalDate.now())
                .purchasePrice(new BigDecimal("35000"))
                .depreciationValue(new BigDecimal("7000"))
                .energyCosts(new BigDecimal("500"))
                .maintenanceCosts(new BigDecimal("200"))
                .insuranceCosts(new BigDecimal("1200"))
                .taxesFees(new BigDecimal("500"))
                .totalCost(new BigDecimal("30000"))
                .analysisPeriodYears(5)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.of(existingTCO));

        // Act
        TCOAnalysisResponse response = tcoAnalysisService.getTCOAnalysis(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getVehicleId());
        assertEquals(new BigDecimal("35000"), response.getPurchasePrice());
        assertEquals(new BigDecimal("7000"), response.getDepreciation());

        verify(vehicleRepository, times(1)).findById(1L);
        verify(tcoAnalysisRepository, times(1)).findLatestByVehicleId(1L);
    }

    @Test
    void testGetTCOAnalysis_NoExistingAnalysis() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.empty());
        when(tripRepository.findByVehicleId(1L)).thenReturn(testTrips);
        when(chargingSessionRepository.findByVehicleId(1L)).thenReturn(testChargingSessions);
        when(maintenanceRecordRepository.findByVehicleId(1L)).thenReturn(testMaintenanceRecords);
        when(tcoAnalysisRepository.save(any(TCOAnalysis.class))).thenAnswer(invocation -> {
            TCOAnalysis tco = invocation.getArgument(0);
            tco.setId(1L);
            return tco;
        });

        // Act
        TCOAnalysisResponse response = tcoAnalysisService.getTCOAnalysis(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getVehicleId());

        verify(tcoAnalysisRepository, times(1)).save(any(TCOAnalysis.class));
    }

    @Test
    void testGetTCOTrend_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(3);
        LocalDate endDate = LocalDate.now();

        TCOAnalysis tco1 = TCOAnalysis.builder()
                .id(1L)
                .vehicleId(1L)
                .analysisDate(startDate)
                .totalCost(new BigDecimal("29000"))
                .build();

        TCOAnalysis tco2 = TCOAnalysis.builder()
                .id(2L)
                .vehicleId(1L)
                .analysisDate(startDate.plusMonths(1))
                .totalCost(new BigDecimal("29500"))
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(tcoAnalysisRepository.findByVehicleIdAndAnalysisDateBetween(1L, startDate, endDate))
                .thenReturn(List.of(tco1, tco2));

        // Act
        List<TCOAnalysisResponse> trend = tcoAnalysisService.getTCOTrend(1L, startDate, endDate);

        // Assert
        assertNotNull(trend);
        assertEquals(2, trend.size());
        assertEquals(new BigDecimal("29000"), trend.get(0).getTotalCost());
        assertEquals(new BigDecimal("29500"), trend.get(1).getTotalCost());

        verify(tcoAnalysisRepository, times(1)).findByVehicleIdAndAnalysisDateBetween(1L, startDate, endDate);
    }

    @Test
    void testRecalculateTCOForAllVehicles_Success() {
        // Arrange
        List<Vehicle> vehicles = List.of(testVehicle);
        when(vehicleRepository.findAll()).thenReturn(vehicles);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(tripRepository.findByVehicleId(1L)).thenReturn(testTrips);
        when(chargingSessionRepository.findByVehicleId(1L)).thenReturn(testChargingSessions);
        when(maintenanceRecordRepository.findByVehicleId(1L)).thenReturn(testMaintenanceRecords);
        when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.empty());
        when(tcoAnalysisRepository.save(any(TCOAnalysis.class))).thenAnswer(invocation -> {
            TCOAnalysis tco = invocation.getArgument(0);
            tco.setId(1L);
            return tco;
        });

        // Act
        tcoAnalysisService.recalculateTCOForAllVehicles();

        // Assert
        verify(vehicleRepository, times(1)).findAll();
        verify(tcoAnalysisRepository, times(1)).save(any(TCOAnalysis.class));
    }
}
