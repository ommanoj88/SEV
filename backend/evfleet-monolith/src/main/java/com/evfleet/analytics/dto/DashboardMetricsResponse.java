package com.evfleet.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard Metrics Response DTO
 * 
 * Lightweight DTO optimized for fast dashboard loading.
 * Contains summary metrics, vehicle status breakdown, and key performance indicators.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetricsResponse {

    // ========== CORE METRICS ==========

    /**
     * Company identifier
     */
    private Long companyId;

    /**
     * Timestamp when metrics were calculated
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Cache status indicator
     */
    private boolean cached;

    /**
     * ETag for conditional requests
     */
    private String etag;

    // ========== FLEET OVERVIEW ==========

    private FleetOverview fleet;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FleetOverview {
        private int totalVehicles;
        private int activeVehicles;
        private int chargingVehicles;
        private int idleVehicles;
        private int maintenanceVehicles;
        private int offlineVehicles;
        private BigDecimal utilizationPercent;
    }

    // ========== VEHICLE STATUS BREAKDOWN ==========

    private StatusBreakdown status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusBreakdown {
        private int driving;
        private int parked;
        private int charging;
        private int maintenance;
        private int offline;
    }

    // ========== BATTERY METRICS ==========

    private BatteryMetrics battery;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatteryMetrics {
        private BigDecimal avgBatteryLevel;
        private int vehiclesLowBattery;  // < 20%
        private int vehiclesCriticalBattery;  // < 10%
        private int vehiclesCharging;
        private int vehiclesFullyCharged;  // >= 90%
    }

    // ========== TODAY'S PERFORMANCE ==========

    private TodayPerformance today;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TodayPerformance {
        private int tripsCompleted;
        private BigDecimal distanceKm;
        private BigDecimal energyConsumedKwh;
        private BigDecimal costRupees;
        private BigDecimal avgEfficiencyKwhPerKm;
        private int alertsCount;
    }

    // ========== WEEKLY COMPARISON ==========

    private WeeklyComparison weeklyTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeeklyComparison {
        private BigDecimal distanceChangePercent;
        private BigDecimal costChangePercent;
        private BigDecimal efficiencyChangePercent;
        private String trend; // UP, DOWN, STABLE
    }

    // ========== QUICK ALERTS ==========

    private List<AlertSummary> alerts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlertSummary {
        private String type; // CRITICAL, WARNING, INFO
        private String message;
        private int count;
        private String priority;
    }

    // ========== TOP VEHICLES ==========

    private List<VehicleSummary> topPerformers;
    private List<VehicleSummary> needsAttention;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleSummary {
        private Long vehicleId;
        private String vehicleNumber;
        private String status;
        private BigDecimal batteryLevel;
        private BigDecimal todayDistanceKm;
        private String issue; // For needs attention list
    }

    // ========== ESG QUICK STATS ==========

    private EsgQuickStats esg;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EsgQuickStats {
        private BigDecimal carbonSavingsKgToday;
        private BigDecimal electrificationPercent;
        private int treesEquivalent;
    }

    // ========== CHARGING INFRASTRUCTURE ==========

    private ChargingStatus charging;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChargingStatus {
        private int totalStations;
        private int stationsAvailable;
        private int stationsInUse;
        private int stationsOffline;
    }
}
