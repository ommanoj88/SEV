package com.evfleet.charging.controller;

import com.evfleet.charging.dto.CostSummaryResponse;
import com.evfleet.charging.dto.StationAnalyticsResponse;
import com.evfleet.charging.dto.UtilizationMetricsResponse;
import com.evfleet.charging.service.ChargingAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ChargingAnalyticsController
 * 
 * @since PR-10 (Charging Analytics)
 */
@WebMvcTest(ChargingAnalyticsController.class)
class ChargingAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChargingAnalyticsService analyticsService;

    private StationAnalyticsResponse stationAnalyticsResponse;
    private UtilizationMetricsResponse utilizationMetricsResponse;
    private CostSummaryResponse costSummaryResponse;
    private DateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = DateTimeFormatter.ISO_DATE_TIME;

        stationAnalyticsResponse = StationAnalyticsResponse.builder()
            .stationId(1L)
            .stationName("Test Station")
            .totalSlots(10)
            .availableSlots(5)
            .totalSessions(50L)
            .completedSessions(45L)
            .activeSessions(5L)
            .utilizationRate(BigDecimal.valueOf(75.50))
            .totalEnergyCharged(BigDecimal.valueOf(1000.0))
            .averageEnergyPerSession(BigDecimal.valueOf(20.0))
            .totalRevenue(BigDecimal.valueOf(300.0))
            .averageSessionCost(BigDecimal.valueOf(6.0))
            .pricePerKwh(BigDecimal.valueOf(0.30))
            .averageSessionDurationMinutes(90L)
            .totalChargingMinutes(4500L)
            .build();

        utilizationMetricsResponse = UtilizationMetricsResponse.builder()
            .totalStations(10)
            .activeStations(8)
            .totalSlots(100)
            .availableSlots(40)
            .occupiedSlots(60)
            .overallUtilizationRate(BigDecimal.valueOf(60.0))
            .averageStationUtilization(BigDecimal.valueOf(55.0))
            .totalSessions(500L)
            .activeSessions(25L)
            .completedSessionsToday(50L)
            .topStations(Collections.emptyList())
            .build();

        costSummaryResponse = CostSummaryResponse.builder()
            .periodStart(LocalDateTime.of(2024, 1, 1, 0, 0))
            .periodEnd(LocalDateTime.of(2024, 1, 31, 23, 59))
            .totalRevenue(BigDecimal.valueOf(5000.0))
            .averageSessionCost(BigDecimal.valueOf(10.0))
            .totalEnergyCharged(BigDecimal.valueOf(15000.0))
            .minSessionCost(BigDecimal.valueOf(5.0))
            .maxSessionCost(BigDecimal.valueOf(50.0))
            .medianSessionCost(BigDecimal.valueOf(10.0))
            .averageEnergyPerSession(BigDecimal.valueOf(30.0))
            .totalSessions(BigDecimal.valueOf(500))
            .completedSessions(BigDecimal.valueOf(480))
            .averagePricePerKwh(BigDecimal.valueOf(0.30))
            .revenuePerKwh(BigDecimal.valueOf(0.33))
            .build();
    }

    @Test
    void testGetStationAnalytics_WithoutDateRange() throws Exception {
        // Arrange
        when(analyticsService.getStationAnalytics(eq(1L))).thenReturn(stationAnalyticsResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/charging/analytics/stations/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationId").value(1))
                .andExpect(jsonPath("$.stationName").value("Test Station"))
                .andExpect(jsonPath("$.totalSlots").value(10))
                .andExpect(jsonPath("$.availableSlots").value(5))
                .andExpect(jsonPath("$.totalSessions").value(50))
                .andExpect(jsonPath("$.utilizationRate").value(75.50))
                .andExpect(jsonPath("$.totalEnergyCharged").value(1000.0))
                .andExpect(jsonPath("$.totalRevenue").value(300.0));
    }

    @Test
    void testGetStationAnalytics_WithDateRange() throws Exception {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
        
        when(analyticsService.getStationAnalytics(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(stationAnalyticsResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/charging/analytics/stations/1")
                .param("startDate", startDate.format(formatter))
                .param("endDate", endDate.format(formatter))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationId").value(1))
                .andExpect(jsonPath("$.stationName").value("Test Station"));
    }

    @Test
    void testGetUtilizationMetrics() throws Exception {
        // Arrange
        when(analyticsService.getUtilizationMetrics()).thenReturn(utilizationMetricsResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/charging/analytics/utilization")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStations").value(10))
                .andExpect(jsonPath("$.activeStations").value(8))
                .andExpect(jsonPath("$.totalSlots").value(100))
                .andExpect(jsonPath("$.availableSlots").value(40))
                .andExpect(jsonPath("$.occupiedSlots").value(60))
                .andExpect(jsonPath("$.overallUtilizationRate").value(60.0))
                .andExpect(jsonPath("$.activeSessions").value(25))
                .andExpect(jsonPath("$.completedSessionsToday").value(50));
    }

    @Test
    void testGetCostSummary() throws Exception {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
        
        when(analyticsService.getCostSummary(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(costSummaryResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/charging/analytics/cost-summary")
                .param("startDate", startDate.format(formatter))
                .param("endDate", endDate.format(formatter))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(5000.0))
                .andExpect(jsonPath("$.averageSessionCost").value(10.0))
                .andExpect(jsonPath("$.totalEnergyCharged").value(15000.0))
                .andExpect(jsonPath("$.minSessionCost").value(5.0))
                .andExpect(jsonPath("$.maxSessionCost").value(50.0))
                .andExpect(jsonPath("$.totalSessions").value(500))
                .andExpect(jsonPath("$.completedSessions").value(480));
    }

    @Test
    void testGetTodayCostSummary() throws Exception {
        // Arrange
        when(analyticsService.getTodayCostSummary()).thenReturn(costSummaryResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/charging/analytics/cost-summary/today")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(5000.0))
                .andExpect(jsonPath("$.averageSessionCost").value(10.0))
                .andExpect(jsonPath("$.totalEnergyCharged").value(15000.0));
    }

    @Test
    void testGetMonthCostSummary() throws Exception {
        // Arrange
        when(analyticsService.getMonthCostSummary()).thenReturn(costSummaryResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/charging/analytics/cost-summary/month")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(5000.0))
                .andExpect(jsonPath("$.averageSessionCost").value(10.0))
                .andExpect(jsonPath("$.totalEnergyCharged").value(15000.0));
    }

    @Test
    void testGetStationAnalytics_InvalidStationId() throws Exception {
        // Arrange
        when(analyticsService.getStationAnalytics(eq(999L)))
            .thenThrow(new RuntimeException("Station not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/charging/analytics/stations/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }
}
