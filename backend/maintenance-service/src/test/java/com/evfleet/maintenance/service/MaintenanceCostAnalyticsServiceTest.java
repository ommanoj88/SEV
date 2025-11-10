package com.evfleet.maintenance.service;

import com.evfleet.maintenance.dto.MaintenanceCostBreakdownDTO;
import com.evfleet.maintenance.dto.MaintenanceCostSummaryDTO;
import com.evfleet.maintenance.dto.VehicleCostComparisonDTO;
import com.evfleet.maintenance.entity.ServiceHistory;
import com.evfleet.maintenance.repository.ServiceHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MaintenanceCostAnalyticsService
 * Tests cost tracking and TCO calculations
 * 
 * @since 2.0.0 (PR 12: Maintenance Cost Tracking)
 */
@ExtendWith(MockitoExtension.class)
class MaintenanceCostAnalyticsServiceTest {

    @Mock
    private ServiceHistoryRepository serviceHistoryRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MaintenanceCostAnalyticsService costAnalyticsService;

    private LocalDate startDate;
    private LocalDate endDate;
    private List<ServiceHistory> mockServiceHistory;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2024, 1, 1);
        endDate = LocalDate.of(2024, 12, 31);
        mockServiceHistory = createMockServiceHistory();
    }

    @Test
    @DisplayName("Should calculate cost summary correctly")
    void testGetCostSummary() {
        // Given
        when(serviceHistoryRepository.findAllInPeriod(startDate, endDate))
                .thenReturn(mockServiceHistory);
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0);
                    Map<String, Object> vehicle = new HashMap<>();
                    if (url.contains("VEH001")) {
                        vehicle.put("fuelType", "EV");
                    } else if (url.contains("VEH002")) {
                        vehicle.put("fuelType", "ICE");
                    } else if (url.contains("VEH003")) {
                        vehicle.put("fuelType", "HYBRID");
                    }
                    return vehicle;
                });

        // When
        MaintenanceCostSummaryDTO summary = costAnalyticsService.getCostSummary(startDate, endDate);

        // Then
        assertNotNull(summary);
        assertEquals(new BigDecimal("4500.00"), summary.getTotalCost());
        assertEquals(new BigDecimal("1500.00"), summary.getEvCost());
        assertEquals(new BigDecimal("2000.00"), summary.getIceCost());
        assertEquals(new BigDecimal("1000.00"), summary.getHybridCost());
        assertEquals(1, summary.getEvVehicleCount());
        assertEquals(1, summary.getIceVehicleCount());
        assertEquals(1, summary.getHybridVehicleCount());
        assertEquals(3, summary.getRecordCount());
        
        verify(serviceHistoryRepository).findAllInPeriod(startDate, endDate);
    }

    @Test
    @DisplayName("Should calculate cost breakdown for specific vehicle")
    void testGetCostBreakdownForVehicle() {
        // Given
        String vehicleId = "VEH001";
        List<ServiceHistory> vehicleHistory = mockServiceHistory.stream()
                .filter(sh -> sh.getVehicleId().equals(vehicleId))
                .toList();
        
        when(serviceHistoryRepository.findByVehicleIdAndServiceDateBetween(
                vehicleId, startDate, endDate))
                .thenReturn(vehicleHistory);
        
        Map<String, Object> vehicle = new HashMap<>();
        vehicle.put("fuelType", "EV");
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(vehicle);

        // When
        MaintenanceCostBreakdownDTO breakdown = costAnalyticsService.getCostBreakdownForVehicle(
                vehicleId, startDate, endDate);

        // Then
        assertNotNull(breakdown);
        assertEquals(vehicleId, breakdown.getVehicleId());
        assertEquals("EV", breakdown.getFuelType());
        assertEquals(new BigDecimal("1500.00"), breakdown.getTotalCost());
        assertEquals(1, breakdown.getRecordCount());
        assertNotNull(breakdown.getCostRecords());
        
        verify(serviceHistoryRepository).findByVehicleIdAndServiceDateBetween(
                vehicleId, startDate, endDate);
    }

    @Test
    @DisplayName("Should compare costs between fuel types")
    void testCompareCostsByFuelType() {
        // Given
        when(serviceHistoryRepository.findAllInPeriod(startDate, endDate))
                .thenReturn(mockServiceHistory);
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0);
                    Map<String, Object> vehicle = new HashMap<>();
                    if (url.contains("VEH001")) {
                        vehicle.put("fuelType", "EV");
                    } else if (url.contains("VEH002")) {
                        vehicle.put("fuelType", "ICE");
                    } else if (url.contains("VEH003")) {
                        vehicle.put("fuelType", "HYBRID");
                    }
                    return vehicle;
                });

        // When
        VehicleCostComparisonDTO comparison = costAnalyticsService.compareCostsByFuelType(
                startDate, endDate);

        // Then
        assertNotNull(comparison);
        assertTrue(comparison.getEvAvgMonthlyCost().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(comparison.getIceAvgMonthlyCost().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(comparison.getHybridAvgMonthlyCost().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(1, comparison.getEvVehicleCount());
        assertEquals(1, comparison.getIceVehicleCount());
        assertEquals(1, comparison.getHybridVehicleCount());
        assertEquals(12, comparison.getMonthsAnalyzed());
        
        verify(serviceHistoryRepository).findAllInPeriod(startDate, endDate);
    }

    @Test
    @DisplayName("Should calculate TCO for vehicle")
    void testCalculateTCO() {
        // Given
        String vehicleId = "VEH001";
        BigDecimal expectedTCO = new BigDecimal("5000.00");
        
        when(serviceHistoryRepository.calculateTotalCostForVehicle(vehicleId))
                .thenReturn(expectedTCO);

        // When
        BigDecimal tco = costAnalyticsService.calculateTCO(vehicleId);

        // Then
        assertNotNull(tco);
        assertEquals(expectedTCO, tco);
        
        verify(serviceHistoryRepository).calculateTotalCostForVehicle(vehicleId);
    }

    @Test
    @DisplayName("Should get cost by category")
    void testGetCostByCategory() {
        // Given
        when(serviceHistoryRepository.findAllInPeriod(startDate, endDate))
                .thenReturn(mockServiceHistory);
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenAnswer(invocation -> {
                    Map<String, Object> vehicle = new HashMap<>();
                    vehicle.put("fuelType", "EV");
                    return vehicle;
                });

        // When
        Map<String, BigDecimal> costByCategory = costAnalyticsService.getCostByCategory(
                startDate, endDate);

        // Then
        assertNotNull(costByCategory);
        assertTrue(costByCategory.containsKey("ICE"));
        assertTrue(costByCategory.containsKey("EV"));
        assertTrue(costByCategory.containsKey("COMMON"));
        
        verify(serviceHistoryRepository).findAllInPeriod(startDate, endDate);
    }

    @Test
    @DisplayName("Should get cost by maintenance type")
    void testGetCostByMaintenanceType() {
        // Given
        when(serviceHistoryRepository.findAllInPeriod(startDate, endDate))
                .thenReturn(mockServiceHistory);
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenAnswer(invocation -> {
                    Map<String, Object> vehicle = new HashMap<>();
                    vehicle.put("fuelType", "EV");
                    return vehicle;
                });

        // When
        Map<String, BigDecimal> costByType = costAnalyticsService.getCostByMaintenanceType(
                startDate, endDate);

        // Then
        assertNotNull(costByType);
        assertFalse(costByType.isEmpty());
        
        verify(serviceHistoryRepository).findAllInPeriod(startDate, endDate);
    }

    @Test
    @DisplayName("Should handle empty service history")
    void testGetCostSummaryWithEmptyHistory() {
        // Given
        when(serviceHistoryRepository.findAllInPeriod(startDate, endDate))
                .thenReturn(Collections.emptyList());

        // When
        MaintenanceCostSummaryDTO summary = costAnalyticsService.getCostSummary(startDate, endDate);

        // Then
        assertNotNull(summary);
        assertEquals(BigDecimal.ZERO, summary.getTotalCost());
        assertEquals(BigDecimal.ZERO, summary.getEvCost());
        assertEquals(BigDecimal.ZERO, summary.getIceCost());
        assertEquals(BigDecimal.ZERO, summary.getHybridCost());
        assertEquals(0, summary.getEvVehicleCount());
        assertEquals(0, summary.getIceVehicleCount());
        assertEquals(0, summary.getHybridVehicleCount());
        assertEquals(0, summary.getRecordCount());
    }

    @Test
    @DisplayName("Should get cost trends over time")
    void testGetCostTrends() {
        // Given
        LocalDate trendStart = LocalDate.of(2024, 1, 1);
        LocalDate trendEnd = LocalDate.of(2024, 3, 31);
        
        when(serviceHistoryRepository.findAllInPeriod(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockServiceHistory);
        
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenAnswer(invocation -> {
                    Map<String, Object> vehicle = new HashMap<>();
                    vehicle.put("fuelType", "EV");
                    return vehicle;
                });

        // When
        Map<String, MaintenanceCostSummaryDTO> trends = costAnalyticsService.getCostTrends(
                trendStart, trendEnd);

        // Then
        assertNotNull(trends);
        assertFalse(trends.isEmpty());
        assertTrue(trends.containsKey("2024-01"));
        assertTrue(trends.containsKey("2024-02"));
        assertTrue(trends.containsKey("2024-03"));
    }

    @Test
    @DisplayName("Should handle vehicle with null costs")
    void testGetCostBreakdownWithNullCosts() {
        // Given
        String vehicleId = "VEH004";
        ServiceHistory historyWithNullCost = new ServiceHistory();
        historyWithNullCost.setId("SH004");
        historyWithNullCost.setVehicleId(vehicleId);
        historyWithNullCost.setServiceDate(LocalDate.now());
        historyWithNullCost.setServiceType("TIRE_ROTATION");
        historyWithNullCost.setCost(null);
        
        when(serviceHistoryRepository.findByVehicleIdAndServiceDateBetween(
                vehicleId, startDate, endDate))
                .thenReturn(List.of(historyWithNullCost));
        
        Map<String, Object> vehicle = new HashMap<>();
        vehicle.put("fuelType", "EV");
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(vehicle);

        // When
        MaintenanceCostBreakdownDTO breakdown = costAnalyticsService.getCostBreakdownForVehicle(
                vehicleId, startDate, endDate);

        // Then
        assertNotNull(breakdown);
        assertEquals(BigDecimal.ZERO, breakdown.getTotalCost());
        assertEquals(BigDecimal.ZERO, breakdown.getAvgCostPerService());
    }

    /**
     * Create mock service history for testing
     */
    private List<ServiceHistory> createMockServiceHistory() {
        List<ServiceHistory> history = new ArrayList<>();
        
        // EV vehicle maintenance
        ServiceHistory evHistory = new ServiceHistory();
        evHistory.setId("SH001");
        evHistory.setVehicleId("VEH001");
        evHistory.setServiceDate(LocalDate.of(2024, 6, 15));
        evHistory.setServiceType("BATTERY_CHECK");
        evHistory.setCost(new BigDecimal("1500.00"));
        evHistory.setServiceCenter("EV Service Center");
        history.add(evHistory);
        
        // ICE vehicle maintenance
        ServiceHistory iceHistory = new ServiceHistory();
        iceHistory.setId("SH002");
        iceHistory.setVehicleId("VEH002");
        iceHistory.setServiceDate(LocalDate.of(2024, 6, 20));
        iceHistory.setServiceType("OIL_CHANGE");
        iceHistory.setCost(new BigDecimal("2000.00"));
        iceHistory.setServiceCenter("ICE Service Center");
        history.add(iceHistory);
        
        // HYBRID vehicle maintenance
        ServiceHistory hybridHistory = new ServiceHistory();
        hybridHistory.setId("SH003");
        hybridHistory.setVehicleId("VEH003");
        hybridHistory.setServiceDate(LocalDate.of(2024, 6, 25));
        hybridHistory.setServiceType("TIRE_ROTATION");
        hybridHistory.setCost(new BigDecimal("1000.00"));
        hybridHistory.setServiceCenter("Hybrid Service Center");
        history.add(hybridHistory);
        
        return history;
    }
}
