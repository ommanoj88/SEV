package com.evfleet.maintenance.service;

import com.evfleet.maintenance.dto.MaintenanceCostBreakdownDTO;
import com.evfleet.maintenance.dto.MaintenanceCostSummaryDTO;
import com.evfleet.maintenance.dto.VehicleCostComparisonDTO;
import com.evfleet.maintenance.entity.ServiceHistory;
import com.evfleet.maintenance.model.MaintenanceType;
import com.evfleet.maintenance.repository.ServiceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MaintenanceCostAnalyticsService
 * Service for tracking and analyzing maintenance costs by fuel type.
 * Provides Total Cost of Ownership (TCO) calculations and comparisons.
 * 
 * @since 2.0.0 (PR 12: Maintenance Cost Tracking)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceCostAnalyticsService {
    
    private final ServiceHistoryRepository serviceHistoryRepository;
    private final RestTemplate restTemplate;
    
    // Fleet service URL - uses service discovery via Eureka
    private static final String FLEET_SERVICE_URL = "http://fleet-service/api/v1/vehicles";
    
    /**
     * Get comprehensive cost summary for all vehicles in a date range
     * 
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @return MaintenanceCostSummaryDTO with aggregated costs
     */
    public MaintenanceCostSummaryDTO getCostSummary(LocalDate startDate, LocalDate endDate) {
        log.info("Calculating cost summary for period {} to {}", startDate, endDate);
        
        List<ServiceHistory> records = serviceHistoryRepository.findAllInPeriod(startDate, endDate);
        
        // Group by vehicle to get fuel types
        Map<String, List<ServiceHistory>> recordsByVehicle = records.stream()
                .collect(Collectors.groupingBy(ServiceHistory::getVehicleId));
        
        BigDecimal evCost = BigDecimal.ZERO;
        BigDecimal iceCost = BigDecimal.ZERO;
        BigDecimal hybridCost = BigDecimal.ZERO;
        
        Set<String> evVehicles = new HashSet<>();
        Set<String> iceVehicles = new HashSet<>();
        Set<String> hybridVehicles = new HashSet<>();
        
        Map<String, BigDecimal> costByMaintenanceType = new HashMap<>();
        Map<String, BigDecimal> costByCategory = new HashMap<>();
        costByCategory.put("ICE", BigDecimal.ZERO);
        costByCategory.put("EV", BigDecimal.ZERO);
        costByCategory.put("COMMON", BigDecimal.ZERO);
        
        // Process each vehicle's records
        for (Map.Entry<String, List<ServiceHistory>> entry : recordsByVehicle.entrySet()) {
            String vehicleId = entry.getKey();
            List<ServiceHistory> vehicleRecords = entry.getValue();
            
            String fuelType = getVehicleFuelType(vehicleId);
            
            BigDecimal vehicleTotalCost = vehicleRecords.stream()
                    .map(ServiceHistory::getCost)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Accumulate costs by fuel type
            if ("EV".equals(fuelType)) {
                evCost = evCost.add(vehicleTotalCost);
                evVehicles.add(vehicleId);
            } else if ("ICE".equals(fuelType)) {
                iceCost = iceCost.add(vehicleTotalCost);
                iceVehicles.add(vehicleId);
            } else if ("HYBRID".equals(fuelType)) {
                hybridCost = hybridCost.add(vehicleTotalCost);
                hybridVehicles.add(vehicleId);
            }
            
            // Accumulate costs by maintenance type
            for (ServiceHistory record : vehicleRecords) {
                String serviceType = record.getServiceType();
                BigDecimal cost = record.getCost() != null ? record.getCost() : BigDecimal.ZERO;
                
                costByMaintenanceType.merge(serviceType, cost, BigDecimal::add);
                
                // Categorize by maintenance category
                try {
                    MaintenanceType maintenanceType = MaintenanceType.valueOf(serviceType);
                    String category = maintenanceType.getCategory();
                    costByCategory.merge(category, cost, BigDecimal::add);
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown maintenance type: {}", serviceType);
                }
            }
        }
        
        BigDecimal totalCost = evCost.add(iceCost).add(hybridCost);
        
        // Calculate averages
        BigDecimal avgCostPerEV = evVehicles.isEmpty() ? BigDecimal.ZERO :
                evCost.divide(BigDecimal.valueOf(evVehicles.size()), 2, RoundingMode.HALF_UP);
        BigDecimal avgCostPerICE = iceVehicles.isEmpty() ? BigDecimal.ZERO :
                iceCost.divide(BigDecimal.valueOf(iceVehicles.size()), 2, RoundingMode.HALF_UP);
        BigDecimal avgCostPerHybrid = hybridVehicles.isEmpty() ? BigDecimal.ZERO :
                hybridCost.divide(BigDecimal.valueOf(hybridVehicles.size()), 2, RoundingMode.HALF_UP);
        
        return MaintenanceCostSummaryDTO.builder()
                .totalCost(totalCost)
                .evCost(evCost)
                .iceCost(iceCost)
                .hybridCost(hybridCost)
                .evVehicleCount(evVehicles.size())
                .iceVehicleCount(iceVehicles.size())
                .hybridVehicleCount(hybridVehicles.size())
                .avgCostPerEV(avgCostPerEV)
                .avgCostPerICE(avgCostPerICE)
                .avgCostPerHybrid(avgCostPerHybrid)
                .costByMaintenanceType(costByMaintenanceType)
                .costByCategory(costByCategory)
                .periodStart(startDate)
                .periodEnd(endDate)
                .recordCount(records.size())
                .build();
    }
    
    /**
     * Get detailed cost breakdown for a specific vehicle
     * 
     * @param vehicleId Vehicle ID
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @return MaintenanceCostBreakdownDTO with detailed breakdown
     */
    public MaintenanceCostBreakdownDTO getCostBreakdownForVehicle(
            String vehicleId, LocalDate startDate, LocalDate endDate) {
        
        log.info("Getting cost breakdown for vehicle {} from {} to {}", vehicleId, startDate, endDate);
        
        List<ServiceHistory> records;
        if (startDate != null && endDate != null) {
            records = serviceHistoryRepository.findByVehicleIdAndServiceDateBetween(
                    vehicleId, startDate, endDate);
        } else {
            records = serviceHistoryRepository.findByVehicleId(vehicleId);
        }
        
        String fuelType = getVehicleFuelType(vehicleId);
        
        BigDecimal totalCost = records.stream()
                .map(ServiceHistory::getCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgCostPerService = records.isEmpty() ? BigDecimal.ZERO :
                totalCost.divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP);
        
        // Find most expensive maintenance
        ServiceHistory mostExpensive = records.stream()
                .filter(r -> r.getCost() != null)
                .max(Comparator.comparing(ServiceHistory::getCost))
                .orElse(null);
        
        String mostExpensiveType = mostExpensive != null ? mostExpensive.getServiceType() : null;
        BigDecimal mostExpensiveCost = mostExpensive != null ? mostExpensive.getCost() : BigDecimal.ZERO;
        
        // Convert to cost records
        List<MaintenanceCostBreakdownDTO.CostRecord> costRecords = records.stream()
                .map(sh -> MaintenanceCostBreakdownDTO.CostRecord.builder()
                        .serviceHistoryId(sh.getId())
                        .serviceDate(sh.getServiceDate())
                        .serviceType(sh.getServiceType())
                        .cost(sh.getCost())
                        .serviceCenter(sh.getServiceCenter())
                        .description(sh.getDescription())
                        .build())
                .collect(Collectors.toList());
        
        return MaintenanceCostBreakdownDTO.builder()
                .vehicleId(vehicleId)
                .fuelType(fuelType)
                .totalCost(totalCost)
                .recordCount(records.size())
                .avgCostPerService(avgCostPerService)
                .mostExpensiveMaintenanceType(mostExpensiveType)
                .mostExpensiveCost(mostExpensiveCost)
                .costRecords(costRecords)
                .build();
    }
    
    /**
     * Compare maintenance costs between fuel types
     * 
     * @param startDate Start date of comparison period
     * @param endDate End date of comparison period
     * @return VehicleCostComparisonDTO with cost comparison
     */
    public VehicleCostComparisonDTO compareCostsByFuelType(LocalDate startDate, LocalDate endDate) {
        log.info("Comparing costs by fuel type for period {} to {}", startDate, endDate);
        
        MaintenanceCostSummaryDTO summary = getCostSummary(startDate, endDate);
        
        long monthsAnalyzed = ChronoUnit.MONTHS.between(startDate, endDate);
        if (monthsAnalyzed == 0) monthsAnalyzed = 1;
        
        // Calculate monthly averages
        BigDecimal evAvgMonthlyCost = summary.getEvVehicleCount() > 0 ?
                summary.getEvCost().divide(BigDecimal.valueOf(monthsAnalyzed), 2, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(summary.getEvVehicleCount()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        BigDecimal iceAvgMonthlyCost = summary.getIceVehicleCount() > 0 ?
                summary.getIceCost().divide(BigDecimal.valueOf(monthsAnalyzed), 2, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(summary.getIceVehicleCount()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        BigDecimal hybridAvgMonthlyCost = summary.getHybridVehicleCount() > 0 ?
                summary.getHybridCost().divide(BigDecimal.valueOf(monthsAnalyzed), 2, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(summary.getHybridVehicleCount()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Calculate savings
        BigDecimal evVsIceSavings = iceAvgMonthlyCost.subtract(evAvgMonthlyCost);
        BigDecimal evVsIceSavingsPercentage = iceAvgMonthlyCost.compareTo(BigDecimal.ZERO) > 0 ?
                evVsIceSavings.divide(iceAvgMonthlyCost, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        BigDecimal hybridVsIceSavings = iceAvgMonthlyCost.subtract(hybridAvgMonthlyCost);
        BigDecimal hybridVsIceSavingsPercentage = iceAvgMonthlyCost.compareTo(BigDecimal.ZERO) > 0 ?
                hybridVsIceSavings.divide(iceAvgMonthlyCost, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        return VehicleCostComparisonDTO.builder()
                .evAvgMonthlyCost(evAvgMonthlyCost)
                .iceAvgMonthlyCost(iceAvgMonthlyCost)
                .hybridAvgMonthlyCost(hybridAvgMonthlyCost)
                .evVsIceSavings(evVsIceSavings)
                .evVsIceSavingsPercentage(evVsIceSavingsPercentage)
                .hybridVsIceSavings(hybridVsIceSavings)
                .hybridVsIceSavingsPercentage(hybridVsIceSavingsPercentage)
                .evVehicleCount(summary.getEvVehicleCount())
                .iceVehicleCount(summary.getIceVehicleCount())
                .hybridVehicleCount(summary.getHybridVehicleCount())
                .monthsAnalyzed((int) monthsAnalyzed)
                .build();
    }
    
    /**
     * Get cost summary by maintenance type category (ICE, EV, COMMON)
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Map of category to total cost
     */
    public Map<String, BigDecimal> getCostByCategory(LocalDate startDate, LocalDate endDate) {
        log.info("Getting cost breakdown by category for period {} to {}", startDate, endDate);
        
        MaintenanceCostSummaryDTO summary = getCostSummary(startDate, endDate);
        return summary.getCostByCategory();
    }
    
    /**
     * Get cost summary by maintenance type
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Map of maintenance type to total cost
     */
    public Map<String, BigDecimal> getCostByMaintenanceType(LocalDate startDate, LocalDate endDate) {
        log.info("Getting cost breakdown by maintenance type for period {} to {}", startDate, endDate);
        
        MaintenanceCostSummaryDTO summary = getCostSummary(startDate, endDate);
        return summary.getCostByMaintenanceType();
    }
    
    /**
     * Calculate Total Cost of Ownership (TCO) for a vehicle
     * 
     * @param vehicleId Vehicle ID
     * @return Total maintenance cost for the vehicle
     */
    public BigDecimal calculateTCO(String vehicleId) {
        log.info("Calculating TCO for vehicle {}", vehicleId);
        
        return serviceHistoryRepository.calculateTotalCostForVehicle(vehicleId);
    }
    
    /**
     * Get vehicle fuel type from fleet service
     * Falls back to "EV" if unable to retrieve
     * 
     * @param vehicleId Vehicle ID
     * @return Fuel type (ICE, EV, HYBRID)
     */
    private String getVehicleFuelType(String vehicleId) {
        try {
            // Try to get vehicle info from fleet service
            String url = FLEET_SERVICE_URL + "/" + vehicleId;
            Map<String, Object> vehicle = restTemplate.getForObject(url, Map.class);
            
            if (vehicle != null && vehicle.containsKey("fuelType")) {
                return (String) vehicle.get("fuelType");
            }
        } catch (HttpClientErrorException e) {
            log.warn("Unable to retrieve vehicle {} from fleet service: {}", vehicleId, e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving vehicle {} fuel type: {}", vehicleId, e.getMessage());
        }
        
        // Default to EV for backward compatibility
        return "EV";
    }
    
    /**
     * Get cost trends over time (monthly breakdown)
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Map of month (YYYY-MM) to cost summary
     */
    public Map<String, MaintenanceCostSummaryDTO> getCostTrends(LocalDate startDate, LocalDate endDate) {
        log.info("Getting cost trends for period {} to {}", startDate, endDate);
        
        Map<String, MaintenanceCostSummaryDTO> trends = new LinkedHashMap<>();
        
        LocalDate currentDate = startDate.withDayOfMonth(1);
        while (!currentDate.isAfter(endDate)) {
            LocalDate monthEnd = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
            if (monthEnd.isAfter(endDate)) {
                monthEnd = endDate;
            }
            
            String monthKey = currentDate.getYear() + "-" + 
                    String.format("%02d", currentDate.getMonthValue());
            
            MaintenanceCostSummaryDTO monthlySummary = getCostSummary(currentDate, monthEnd);
            trends.put(monthKey, monthlySummary);
            
            currentDate = currentDate.plusMonths(1);
        }
        
        return trends;
    }
}
