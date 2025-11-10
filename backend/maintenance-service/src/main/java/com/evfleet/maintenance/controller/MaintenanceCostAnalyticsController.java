package com.evfleet.maintenance.controller;

import com.evfleet.maintenance.dto.MaintenanceCostBreakdownDTO;
import com.evfleet.maintenance.dto.MaintenanceCostSummaryDTO;
import com.evfleet.maintenance.dto.VehicleCostComparisonDTO;
import com.evfleet.maintenance.service.MaintenanceCostAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * MaintenanceCostAnalyticsController
 * REST API endpoints for maintenance cost tracking and analytics.
 * Supports Total Cost of Ownership (TCO) calculations.
 * 
 * @since 2.0.0 (PR 12: Maintenance Cost Tracking)
 */
@RestController
@RequestMapping("/api/v1/maintenance/cost-analytics")
@RequiredArgsConstructor
@Tag(name = "Maintenance Cost Analytics", description = "Cost tracking and TCO calculation endpoints")
public class MaintenanceCostAnalyticsController {
    
    private final MaintenanceCostAnalyticsService costAnalyticsService;
    
    /**
     * Get comprehensive cost summary for all vehicles
     * 
     * @param startDate Start date of the period (defaults to 1 year ago)
     * @param endDate End date of the period (defaults to today)
     * @return MaintenanceCostSummaryDTO with aggregated costs
     */
    @GetMapping("/summary")
    @Operation(
        summary = "Get cost summary",
        description = "Get comprehensive maintenance cost summary for all vehicles in a date range, " +
                     "categorized by fuel type (EV, ICE, HYBRID)"
    )
    public ResponseEntity<MaintenanceCostSummaryDTO> getCostSummary(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusYears(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        MaintenanceCostSummaryDTO summary = costAnalyticsService.getCostSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get detailed cost breakdown for a specific vehicle
     * 
     * @param vehicleId Vehicle ID
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @return MaintenanceCostBreakdownDTO with detailed breakdown
     */
    @GetMapping("/vehicle/{vehicleId}")
    @Operation(
        summary = "Get vehicle cost breakdown",
        description = "Get detailed maintenance cost breakdown for a specific vehicle"
    )
    public ResponseEntity<MaintenanceCostBreakdownDTO> getVehicleCostBreakdown(
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable String vehicleId,
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        MaintenanceCostBreakdownDTO breakdown = costAnalyticsService.getCostBreakdownForVehicle(
                vehicleId, startDate, endDate);
        return ResponseEntity.ok(breakdown);
    }
    
    /**
     * Compare maintenance costs between fuel types
     * 
     * @param startDate Start date of comparison period (defaults to 1 year ago)
     * @param endDate End date of comparison period (defaults to today)
     * @return VehicleCostComparisonDTO with cost comparison
     */
    @GetMapping("/compare")
    @Operation(
        summary = "Compare costs by fuel type",
        description = "Compare maintenance costs between EV, ICE, and HYBRID vehicles. " +
                     "Useful for Total Cost of Ownership (TCO) analysis."
    )
    public ResponseEntity<VehicleCostComparisonDTO> compareCostsByFuelType(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusYears(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        VehicleCostComparisonDTO comparison = costAnalyticsService.compareCostsByFuelType(
                startDate, endDate);
        return ResponseEntity.ok(comparison);
    }
    
    /**
     * Get cost breakdown by maintenance category (ICE, EV, COMMON)
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Map of category to total cost
     */
    @GetMapping("/by-category")
    @Operation(
        summary = "Get costs by category",
        description = "Get maintenance costs grouped by category: ICE-specific, EV-specific, or COMMON"
    )
    public ResponseEntity<Map<String, BigDecimal>> getCostByCategory(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusYears(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        Map<String, BigDecimal> costByCategory = costAnalyticsService.getCostByCategory(
                startDate, endDate);
        return ResponseEntity.ok(costByCategory);
    }
    
    /**
     * Get cost breakdown by maintenance type
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Map of maintenance type to total cost
     */
    @GetMapping("/by-maintenance-type")
    @Operation(
        summary = "Get costs by maintenance type",
        description = "Get maintenance costs grouped by specific maintenance type " +
                     "(e.g., OIL_CHANGE, BATTERY_CHECK, TIRE_ROTATION)"
    )
    public ResponseEntity<Map<String, BigDecimal>> getCostByMaintenanceType(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusYears(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        Map<String, BigDecimal> costByType = costAnalyticsService.getCostByMaintenanceType(
                startDate, endDate);
        return ResponseEntity.ok(costByType);
    }
    
    /**
     * Calculate Total Cost of Ownership (TCO) for a vehicle
     * 
     * @param vehicleId Vehicle ID
     * @return Total maintenance cost
     */
    @GetMapping("/tco/{vehicleId}")
    @Operation(
        summary = "Calculate TCO for vehicle",
        description = "Calculate Total Cost of Ownership (TCO) - total maintenance cost for a vehicle"
    )
    public ResponseEntity<Map<String, BigDecimal>> calculateTCO(
            @Parameter(description = "Vehicle ID", required = true)
            @PathVariable String vehicleId) {
        
        BigDecimal tco = costAnalyticsService.calculateTCO(vehicleId);
        return ResponseEntity.ok(Map.of("vehicleId", new BigDecimal(vehicleId), "totalMaintenanceCost", tco));
    }
    
    /**
     * Get cost trends over time (monthly breakdown)
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Map of month to cost summary
     */
    @GetMapping("/trends")
    @Operation(
        summary = "Get cost trends",
        description = "Get monthly maintenance cost trends over a period"
    )
    public ResponseEntity<Map<String, MaintenanceCostSummaryDTO>> getCostTrends(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusYears(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        Map<String, MaintenanceCostSummaryDTO> trends = costAnalyticsService.getCostTrends(
                startDate, endDate);
        return ResponseEntity.ok(trends);
    }
}
