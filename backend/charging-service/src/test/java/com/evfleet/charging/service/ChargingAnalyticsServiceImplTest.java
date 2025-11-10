package com.evfleet.charging.service;

import com.evfleet.charging.dto.CostSummaryResponse;
import com.evfleet.charging.dto.StationAnalyticsResponse;
import com.evfleet.charging.dto.UtilizationMetricsResponse;
import com.evfleet.charging.entity.ChargingSession;
import com.evfleet.charging.entity.ChargingStation;
import com.evfleet.charging.exception.ResourceNotFoundException;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.charging.repository.ChargingStationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChargingAnalyticsServiceImpl
 * 
 * @since PR-10 (Charging Analytics)
 */
@ExtendWith(MockitoExtension.class)
class ChargingAnalyticsServiceImplTest {

    @Mock
    private ChargingSessionRepository sessionRepository;

    @Mock
    private ChargingStationRepository stationRepository;

    @InjectMocks
    private ChargingAnalyticsServiceImpl analyticsService;

    private ChargingStation testStation;
    private ChargingSession testSession;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        testStation = new ChargingStation();
        testStation.setId(1L);
        testStation.setName("Test Station");
        testStation.setTotalSlots(10);
        testStation.setAvailableSlots(5);
        testStation.setPricePerKwh(BigDecimal.valueOf(0.30));
        testStation.setStatus(ChargingStation.StationStatus.ACTIVE);

        testSession = new ChargingSession();
        testSession.setId(1L);
        testSession.setVehicleId(100L);
        testSession.setStationId(1L);
        testSession.setStartTime(LocalDateTime.now().minusHours(2));
        testSession.setEndTime(LocalDateTime.now());
        testSession.setEnergyConsumed(BigDecimal.valueOf(50.0));
        testSession.setCost(BigDecimal.valueOf(15.0));
        testSession.setStartBatteryLevel(BigDecimal.valueOf(20.0));
        testSession.setEndBatteryLevel(BigDecimal.valueOf(80.0));
        testSession.setStatus(ChargingSession.SessionStatus.COMPLETED);
        testSession.setDurationMinutes(120L);
    }

    @Test
    void testGetStationAnalytics_Success() {
        // Arrange
        when(stationRepository.findById(1L)).thenReturn(Optional.of(testStation));
        when(sessionRepository.findByStationIdAndDateRange(eq(1L), any(), any()))
            .thenReturn(Arrays.asList(testSession));
        when(sessionRepository.countActiveSessionsByStation(1L)).thenReturn(2L);
        when(sessionRepository.getTotalEnergyByStation(eq(1L), any(), any()))
            .thenReturn(BigDecimal.valueOf(100.0));
        when(sessionRepository.getTotalCostByStation(eq(1L), any(), any()))
            .thenReturn(BigDecimal.valueOf(30.0));
        when(sessionRepository.getAverageDurationByStation(eq(1L), any(), any()))
            .thenReturn(90.0);

        // Act
        StationAnalyticsResponse response = analyticsService.getStationAnalytics(1L, startDate, endDate);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getStationId());
        assertEquals("Test Station", response.getStationName());
        assertEquals(10, response.getTotalSlots());
        assertEquals(5, response.getAvailableSlots());
        assertEquals(2L, response.getActiveSessions());
        assertEquals(BigDecimal.valueOf(100.0), response.getTotalEnergyCharged());
        assertEquals(BigDecimal.valueOf(30.0), response.getTotalRevenue());

        verify(stationRepository, times(1)).findById(1L);
        verify(sessionRepository, times(1)).findByStationIdAndDateRange(eq(1L), any(), any());
    }

    @Test
    void testGetStationAnalytics_StationNotFound() {
        // Arrange
        when(stationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            analyticsService.getStationAnalytics(999L, startDate, endDate);
        });

        verify(stationRepository, times(1)).findById(999L);
    }

    @Test
    void testGetStationAnalytics_NoSessions() {
        // Arrange
        when(stationRepository.findById(1L)).thenReturn(Optional.of(testStation));
        when(sessionRepository.findByStationIdAndDateRange(eq(1L), any(), any()))
            .thenReturn(Collections.emptyList());
        when(sessionRepository.countActiveSessionsByStation(1L)).thenReturn(0L);
        when(sessionRepository.getTotalEnergyByStation(eq(1L), any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(sessionRepository.getTotalCostByStation(eq(1L), any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(sessionRepository.getAverageDurationByStation(eq(1L), any(), any()))
            .thenReturn(0.0);

        // Act
        StationAnalyticsResponse response = analyticsService.getStationAnalytics(1L, startDate, endDate);

        // Assert
        assertNotNull(response);
        assertEquals(0L, response.getTotalSessions());
        assertEquals(BigDecimal.ZERO, response.getTotalEnergyCharged());
        assertEquals(BigDecimal.ZERO, response.getTotalRevenue());
    }

    @Test
    void testGetUtilizationMetrics_Success() {
        // Arrange
        ChargingStation station1 = new ChargingStation();
        station1.setId(1L);
        station1.setName("Station 1");
        station1.setTotalSlots(10);
        station1.setAvailableSlots(5);
        station1.setStatus(ChargingStation.StationStatus.ACTIVE);

        ChargingStation station2 = new ChargingStation();
        station2.setId(2L);
        station2.setName("Station 2");
        station2.setTotalSlots(8);
        station2.setAvailableSlots(2);
        station2.setStatus(ChargingStation.StationStatus.ACTIVE);

        when(stationRepository.findAll()).thenReturn(Arrays.asList(station1, station2));
        when(sessionRepository.countAllActiveSessions()).thenReturn(5L);
        when(sessionRepository.findCompletedSessionsInRange(any(), any()))
            .thenReturn(Arrays.asList(testSession));
        when(sessionRepository.findAll()).thenReturn(Arrays.asList(testSession));
        when(sessionRepository.countCompletedSessionsByStation(anyLong())).thenReturn(10L);

        // Act
        UtilizationMetricsResponse response = analyticsService.getUtilizationMetrics();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getTotalStations());
        assertEquals(2, response.getActiveStations());
        assertEquals(18, response.getTotalSlots());
        assertEquals(7, response.getAvailableSlots());
        assertEquals(11, response.getOccupiedSlots());
        assertEquals(5L, response.getActiveSessions());
        assertNotNull(response.getTopStations());
    }

    @Test
    void testGetUtilizationMetrics_NoStations() {
        // Arrange
        when(stationRepository.findAll()).thenReturn(Collections.emptyList());
        when(sessionRepository.countAllActiveSessions()).thenReturn(0L);
        when(sessionRepository.findCompletedSessionsInRange(any(), any()))
            .thenReturn(Collections.emptyList());
        when(sessionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        UtilizationMetricsResponse response = analyticsService.getUtilizationMetrics();

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getTotalStations());
        assertEquals(0, response.getTotalSlots());
        assertEquals(BigDecimal.ZERO, response.getOverallUtilizationRate());
    }

    @Test
    void testGetCostSummary_Success() {
        // Arrange
        ChargingSession session1 = new ChargingSession();
        session1.setCost(BigDecimal.valueOf(10.0));
        session1.setEnergyConsumed(BigDecimal.valueOf(30.0));
        session1.setStatus(ChargingSession.SessionStatus.COMPLETED);
        session1.setStartTime(startDate.plusDays(1));

        ChargingSession session2 = new ChargingSession();
        session2.setCost(BigDecimal.valueOf(20.0));
        session2.setEnergyConsumed(BigDecimal.valueOf(60.0));
        session2.setStatus(ChargingSession.SessionStatus.COMPLETED);
        session2.setStartTime(startDate.plusDays(2));

        when(sessionRepository.findCompletedSessionsInRange(startDate, endDate))
            .thenReturn(Arrays.asList(session1, session2));
        when(sessionRepository.getTotalRevenue(startDate, endDate))
            .thenReturn(BigDecimal.valueOf(30.0));
        when(sessionRepository.getTotalEnergyCharged(startDate, endDate))
            .thenReturn(BigDecimal.valueOf(90.0));
        when(sessionRepository.findAll()).thenReturn(Arrays.asList(session1, session2));
        when(stationRepository.findAll()).thenReturn(Arrays.asList(testStation));

        // Act
        CostSummaryResponse response = analyticsService.getCostSummary(startDate, endDate);

        // Assert
        assertNotNull(response);
        assertEquals(startDate, response.getPeriodStart());
        assertEquals(endDate, response.getPeriodEnd());
        assertEquals(BigDecimal.valueOf(30.0), response.getTotalRevenue());
        assertEquals(BigDecimal.valueOf(90.0), response.getTotalEnergyCharged());
        assertTrue(response.getAverageSessionCost().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGetCostSummary_NoSessions() {
        // Arrange
        when(sessionRepository.findCompletedSessionsInRange(startDate, endDate))
            .thenReturn(Collections.emptyList());
        when(sessionRepository.getTotalRevenue(startDate, endDate))
            .thenReturn(BigDecimal.ZERO);
        when(sessionRepository.getTotalEnergyCharged(startDate, endDate))
            .thenReturn(BigDecimal.ZERO);
        when(sessionRepository.findAll()).thenReturn(Collections.emptyList());
        when(stationRepository.findAll()).thenReturn(Arrays.asList(testStation));

        // Act
        CostSummaryResponse response = analyticsService.getCostSummary(startDate, endDate);

        // Assert
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getTotalRevenue());
        assertEquals(BigDecimal.ZERO, response.getTotalEnergyCharged());
        assertEquals(BigDecimal.ZERO, response.getAverageSessionCost());
    }

    @Test
    void testGetTodayCostSummary_Success() {
        // Arrange
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        when(sessionRepository.findCompletedSessionsInRange(any(), any()))
            .thenReturn(Arrays.asList(testSession));
        when(sessionRepository.getTotalRevenue(any(), any()))
            .thenReturn(BigDecimal.valueOf(15.0));
        when(sessionRepository.getTotalEnergyCharged(any(), any()))
            .thenReturn(BigDecimal.valueOf(50.0));
        when(sessionRepository.findAll()).thenReturn(Arrays.asList(testSession));
        when(stationRepository.findAll()).thenReturn(Arrays.asList(testStation));

        // Act
        CostSummaryResponse response = analyticsService.getTodayCostSummary();

        // Assert
        assertNotNull(response);
        assertNotNull(response.getPeriodStart());
        assertNotNull(response.getPeriodEnd());
    }

    @Test
    void testGetMonthCostSummary_Success() {
        // Arrange
        when(sessionRepository.findCompletedSessionsInRange(any(), any()))
            .thenReturn(Arrays.asList(testSession));
        when(sessionRepository.getTotalRevenue(any(), any()))
            .thenReturn(BigDecimal.valueOf(15.0));
        when(sessionRepository.getTotalEnergyCharged(any(), any()))
            .thenReturn(BigDecimal.valueOf(50.0));
        when(sessionRepository.findAll()).thenReturn(Arrays.asList(testSession));
        when(stationRepository.findAll()).thenReturn(Arrays.asList(testStation));

        // Act
        CostSummaryResponse response = analyticsService.getMonthCostSummary();

        // Assert
        assertNotNull(response);
        assertNotNull(response.getPeriodStart());
        assertNotNull(response.getPeriodEnd());
    }

    @Test
    void testGetStationAnalytics_WithAllTimeData() {
        // Arrange
        when(stationRepository.findById(1L)).thenReturn(Optional.of(testStation));
        when(sessionRepository.findByStationIdAndDateRange(eq(1L), any(), any()))
            .thenReturn(Arrays.asList(testSession));
        when(sessionRepository.countActiveSessionsByStation(1L)).thenReturn(1L);
        when(sessionRepository.getTotalEnergyByStation(eq(1L), any(), any()))
            .thenReturn(BigDecimal.valueOf(50.0));
        when(sessionRepository.getTotalCostByStation(eq(1L), any(), any()))
            .thenReturn(BigDecimal.valueOf(15.0));
        when(sessionRepository.getAverageDurationByStation(eq(1L), any(), any()))
            .thenReturn(120.0);

        // Act
        StationAnalyticsResponse response = analyticsService.getStationAnalytics(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getStationId());
        verify(stationRepository, times(1)).findById(1L);
    }
}
