package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.DashboardMetricsResponse;
import com.evfleet.analytics.dto.DashboardMetricsResponse.*;
import com.evfleet.analytics.dto.LiveVehiclePositionResponse;
import com.evfleet.analytics.dto.LiveVehiclePositionResponse.*;
import com.evfleet.analytics.model.FleetSummary;
import com.evfleet.analytics.repository.FleetSummaryRepository;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Dashboard Metrics Service
 * 
 * Provides optimized, cached dashboard metrics for fast loading.
 * Implements in-memory caching with TTL, ETag support, and compression-ready data.
 * 
 * Key Features:
 * - In-memory cache with 5-minute TTL
 * - ETag generation for conditional requests
 * - Lightweight DTOs for minimal payload
 * - Real-time vehicle position tracking
 * - Performance metrics via Micrometer
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class DashboardMetricsService {

    private final VehicleRepository vehicleRepository;
    private final FleetSummaryRepository fleetSummaryRepository;
    private final MeterRegistry meterRegistry;

    // In-memory cache for dashboard metrics
    private final Map<Long, CachedMetrics> metricsCache = new ConcurrentHashMap<>();
    private final Map<Long, CachedPositions> positionsCache = new ConcurrentHashMap<>();

    // Cache TTL settings (in milliseconds)
    private static final long METRICS_CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
    private static final long POSITIONS_CACHE_TTL_MS = 30 * 1000;   // 30 seconds

    // Metrics
    private final Counter cacheHits;
    private final Counter cacheMisses;
    private final Timer metricsLoadTime;

    // Battery thresholds
    private static final BigDecimal BATTERY_CRITICAL = new BigDecimal("10");
    private static final BigDecimal BATTERY_LOW = new BigDecimal("20");
    private static final BigDecimal BATTERY_FULL = new BigDecimal("90");

    @Autowired
    public DashboardMetricsService(
            VehicleRepository vehicleRepository,
            FleetSummaryRepository fleetSummaryRepository,
            MeterRegistry meterRegistry) {
        this.vehicleRepository = vehicleRepository;
        this.fleetSummaryRepository = fleetSummaryRepository;
        this.meterRegistry = meterRegistry;

        this.cacheHits = Counter.builder("dashboard.cache.hits")
                .description("Dashboard metrics cache hits")
                .register(meterRegistry);

        this.cacheMisses = Counter.builder("dashboard.cache.misses")
                .description("Dashboard metrics cache misses")
                .register(meterRegistry);

        this.metricsLoadTime = Timer.builder("dashboard.metrics.load.time")
                .description("Time to load dashboard metrics")
                .register(meterRegistry);
    }

    // ========== CACHED METRICS RETRIEVAL ==========

    /**
     * Get cached dashboard summary metrics
     * Uses in-memory cache with 5-minute TTL
     */
    @Timed(value = "dashboard.getSummary", description = "Get dashboard summary")
    @Transactional(readOnly = true)
    public DashboardMetricsResponse getSummaryMetrics(Long companyId, String requestEtag) {
        log.debug("Getting dashboard summary for company {}", companyId);

        // Check cache
        CachedMetrics cached = metricsCache.get(companyId);
        if (cached != null && !cached.isExpired()) {
            cacheHits.increment();
            
            // Check ETag for 304 Not Modified
            if (requestEtag != null && requestEtag.equals(cached.etag)) {
                log.debug("Cache hit with matching ETag for company {}", companyId);
                return null; // Signal 304 response
            }
            
            log.debug("Cache hit for company {}", companyId);
            cached.metrics.setCached(true);
            return cached.metrics;
        }

        cacheMisses.increment();
        log.debug("Cache miss for company {}, computing metrics", companyId);

        // Compute fresh metrics
        return metricsLoadTime.record(() -> computeAndCacheMetrics(companyId));
    }

    /**
     * Get live vehicle positions
     * Uses in-memory cache with 30-second TTL
     */
    @Timed(value = "dashboard.getLivePositions", description = "Get live vehicle positions")
    @Transactional(readOnly = true)
    public LiveVehiclePositionResponse getLivePositions(Long companyId) {
        log.debug("Getting live positions for company {}", companyId);

        // Check cache
        CachedPositions cached = positionsCache.get(companyId);
        if (cached != null && !cached.isExpired()) {
            cacheHits.increment();
            return cached.positions;
        }

        cacheMisses.increment();
        return computeAndCachePositions(companyId);
    }

    // ========== FORCE REFRESH ==========

    /**
     * Force refresh dashboard metrics cache for a company
     */
    @CacheEvict(value = "dashboardMetrics", key = "#companyId")
    public DashboardMetricsResponse refreshMetrics(Long companyId) {
        log.info("Force refreshing metrics cache for company {}", companyId);
        metricsCache.remove(companyId);
        return computeAndCacheMetrics(companyId);
    }

    /**
     * Force refresh all dashboard caches
     */
    @CacheEvict(value = "dashboardMetrics", allEntries = true)
    public void refreshAllMetrics() {
        log.info("Force refreshing all metrics caches");
        metricsCache.clear();
        positionsCache.clear();
    }

    // ========== CACHE CLEANUP ==========

    /**
     * Scheduled task to clean expired cache entries
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void cleanExpiredCaches() {
        long now = System.currentTimeMillis();
        
        metricsCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        positionsCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // ========== PRIVATE METHODS ==========

    private DashboardMetricsResponse computeAndCacheMetrics(Long companyId) {
        LocalDateTime now = LocalDateTime.now();
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);

        // Compute metrics
        FleetOverview fleet = computeFleetOverview(vehicles);
        StatusBreakdown status = computeStatusBreakdown(vehicles);
        BatteryMetrics battery = computeBatteryMetrics(vehicles);
        TodayPerformance today = computeTodayPerformance(companyId);
        WeeklyComparison weekly = computeWeeklyComparison(companyId);
        List<AlertSummary> alerts = computeAlerts(vehicles);
        EsgQuickStats esg = computeEsgStats(vehicles);
        ChargingStatus charging = computeChargingStatus(companyId);
        
        List<VehicleSummary> topPerformers = getTopPerformers(vehicles);
        List<VehicleSummary> needsAttention = getVehiclesNeedingAttention(vehicles);

        // Generate ETag
        String etag = generateEtag(companyId, now);

        DashboardMetricsResponse response = DashboardMetricsResponse.builder()
                .companyId(companyId)
                .timestamp(now)
                .cached(false)
                .etag(etag)
                .fleet(fleet)
                .status(status)
                .battery(battery)
                .today(today)
                .weeklyTrend(weekly)
                .alerts(alerts)
                .topPerformers(topPerformers)
                .needsAttention(needsAttention)
                .esg(esg)
                .charging(charging)
                .build();

        // Cache the result
        metricsCache.put(companyId, new CachedMetrics(response, etag));
        
        return response;
    }

    private LiveVehiclePositionResponse computeAndCachePositions(Long companyId) {
        LocalDateTime now = LocalDateTime.now();
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);

        List<VehiclePosition> positions = vehicles.stream()
                .map(this::mapToPosition)
                .collect(Collectors.toList());

        // Compute clusters for zoomed out view
        List<VehicleCluster> clusters = computeClusters(positions);

        LiveVehiclePositionResponse response = LiveVehiclePositionResponse.builder()
                .companyId(companyId)
                .timestamp(now)
                .totalVehicles(vehicles.size())
                .trackableVehicles(positions.size())
                .vehicles(positions)
                .clusters(clusters)
                .geofenceAlerts(new ArrayList<>()) // Would come from geofence service
                .build();

        // Cache the result
        positionsCache.put(companyId, new CachedPositions(response));

        return response;
    }

    private FleetOverview computeFleetOverview(List<Vehicle> vehicles) {
        int total = vehicles.size();
        int active = 0, charging = 0, inTrip = 0, maintenance = 0, offline = 0;

        for (Vehicle v : vehicles) {
            if (v.getStatus() == null) {
                offline++;
                continue;
            }
            switch (v.getStatus()) {
                case ACTIVE:
                    active++;
                    break;
                case CHARGING:
                    charging++;
                    break;
                case IN_TRIP:
                    inTrip++;
                    break;
                case MAINTENANCE:
                    maintenance++;
                    break;
                case INACTIVE:
                    offline++;
                    break;
                default:
                    offline++;
            }
        }

        BigDecimal utilization = total > 0
                ? BigDecimal.valueOf((active + inTrip) * 100.0 / total).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return FleetOverview.builder()
                .totalVehicles(total)
                .activeVehicles(active)
                .chargingVehicles(charging)
                .idleVehicles(inTrip) // Map IN_TRIP to idle slot for backward compatibility
                .maintenanceVehicles(maintenance)
                .offlineVehicles(offline)
                .utilizationPercent(utilization)
                .build();
    }

    private StatusBreakdown computeStatusBreakdown(List<Vehicle> vehicles) {
        int driving = 0, parked = 0, charging = 0, maintenance = 0, offline = 0;

        for (Vehicle v : vehicles) {
            if (v.getStatus() == null) {
                offline++;
                continue;
            }
            switch (v.getStatus()) {
                case ACTIVE:
                    driving++;
                    break;
                case IN_TRIP:
                    driving++;
                    break;
                case CHARGING:
                    charging++;
                    break;
                case MAINTENANCE:
                    maintenance++;
                    break;
                case INACTIVE:
                    parked++;
                    break;
                default:
                    offline++;
            }
        }

        return StatusBreakdown.builder()
                .driving(driving)
                .parked(parked)
                .charging(charging)
                .maintenance(maintenance)
                .offline(offline)
                .build();
    }

    private BatteryMetrics computeBatteryMetrics(List<Vehicle> vehicles) {
        List<Vehicle> evVehicles = vehicles.stream()
                .filter(v -> v.getFuelType() == FuelType.EV || v.getFuelType() == FuelType.HYBRID)
                .collect(Collectors.toList());

        if (evVehicles.isEmpty()) {
            return BatteryMetrics.builder()
                    .avgBatteryLevel(BigDecimal.ZERO)
                    .vehiclesLowBattery(0)
                    .vehiclesCriticalBattery(0)
                    .vehiclesCharging(0)
                    .vehiclesFullyCharged(0)
                    .build();
        }

        BigDecimal totalBattery = BigDecimal.ZERO;
        int lowBattery = 0, criticalBattery = 0, charging = 0, fullyCharged = 0;

        for (Vehicle v : evVehicles) {
            BigDecimal battery = v.getCurrentBatterySoc() != null 
                    ? BigDecimal.valueOf(v.getCurrentBatterySoc()) 
                    : BigDecimal.valueOf(50); // Default
            
            totalBattery = totalBattery.add(battery);

            if (battery.compareTo(BATTERY_CRITICAL) < 0) {
                criticalBattery++;
                lowBattery++;
            } else if (battery.compareTo(BATTERY_LOW) < 0) {
                lowBattery++;
            }
            if (battery.compareTo(BATTERY_FULL) >= 0) {
                fullyCharged++;
            }
            if (v.getStatus() == Vehicle.VehicleStatus.CHARGING) {
                charging++;
            }
        }

        BigDecimal avgBattery = totalBattery.divide(
                BigDecimal.valueOf(evVehicles.size()), 1, RoundingMode.HALF_UP);

        return BatteryMetrics.builder()
                .avgBatteryLevel(avgBattery)
                .vehiclesLowBattery(lowBattery)
                .vehiclesCriticalBattery(criticalBattery)
                .vehiclesCharging(charging)
                .vehiclesFullyCharged(fullyCharged)
                .build();
    }

    private TodayPerformance computeTodayPerformance(Long companyId) {
        LocalDate today = LocalDate.now();
        
        // Try to get today's fleet summary
        Optional<FleetSummary> summaryOpt = fleetSummaryRepository
                .findByCompanyIdAndSummaryDate(companyId, today);

        if (summaryOpt.isPresent()) {
            FleetSummary summary = summaryOpt.get();
            BigDecimal efficiency = BigDecimal.ZERO;
            if (summary.getTotalDistance() != null && summary.getTotalDistance() > 0 
                    && summary.getTotalEnergyConsumed() != null) {
                efficiency = summary.getTotalEnergyConsumed()
                        .divide(BigDecimal.valueOf(summary.getTotalDistance()), 3, RoundingMode.HALF_UP);
            }

            return TodayPerformance.builder()
                    .tripsCompleted(summary.getTotalTrips() != null ? summary.getTotalTrips() : 0)
                    .distanceKm(BigDecimal.valueOf(summary.getTotalDistance() != null ? summary.getTotalDistance() : 0))
                    .energyConsumedKwh(summary.getTotalEnergyConsumed() != null ? summary.getTotalEnergyConsumed() : BigDecimal.ZERO)
                    .costRupees(summary.getTotalCost() != null ? summary.getTotalCost() : BigDecimal.ZERO)
                    .avgEfficiencyKwhPerKm(efficiency)
                    .alertsCount(0) // Would come from alerts service
                    .build();
        }

        // Return default/estimated values
        return TodayPerformance.builder()
                .tripsCompleted(0)
                .distanceKm(BigDecimal.ZERO)
                .energyConsumedKwh(BigDecimal.ZERO)
                .costRupees(BigDecimal.ZERO)
                .avgEfficiencyKwhPerKm(BigDecimal.ZERO)
                .alertsCount(0)
                .build();
    }

    private WeeklyComparison computeWeeklyComparison(Long companyId) {
        LocalDate today = LocalDate.now();
        LocalDate lastWeek = today.minusDays(7);

        // Get this week's and last week's data
        List<FleetSummary> thisWeek = fleetSummaryRepository
                .findByCompanyIdAndSummaryDateBetween(companyId, today.minusDays(6), today);
        List<FleetSummary> previousWeek = fleetSummaryRepository
                .findByCompanyIdAndSummaryDateBetween(companyId, lastWeek.minusDays(6), lastWeek);

        BigDecimal thisWeekDistance = sumDistance(thisWeek);
        BigDecimal prevWeekDistance = sumDistance(previousWeek);
        BigDecimal thisWeekCost = sumCost(thisWeek);
        BigDecimal prevWeekCost = sumCost(previousWeek);

        BigDecimal distanceChange = calculateChangePercent(thisWeekDistance, prevWeekDistance);
        BigDecimal costChange = calculateChangePercent(thisWeekCost, prevWeekCost);

        String trend = "STABLE";
        if (distanceChange.compareTo(new BigDecimal("5")) > 0) trend = "UP";
        else if (distanceChange.compareTo(new BigDecimal("-5")) < 0) trend = "DOWN";

        return WeeklyComparison.builder()
                .distanceChangePercent(distanceChange)
                .costChangePercent(costChange)
                .efficiencyChangePercent(BigDecimal.ZERO) // Would need energy data
                .trend(trend)
                .build();
    }

    private List<AlertSummary> computeAlerts(List<Vehicle> vehicles) {
        List<AlertSummary> alerts = new ArrayList<>();

        // Count vehicles with low battery
        long lowBatteryCount = vehicles.stream()
                .filter(v -> v.getFuelType() == FuelType.EV || v.getFuelType() == FuelType.HYBRID)
                .filter(v -> v.getBatteryLevel() != null && v.getBatteryLevel().compareTo(BATTERY_LOW) < 0)
                .count();

        if (lowBatteryCount > 0) {
            alerts.add(AlertSummary.builder()
                    .type("WARNING")
                    .message("Vehicles with low battery")
                    .count((int) lowBatteryCount)
                    .priority("HIGH")
                    .build());
        }

        // Count vehicles in maintenance
        long maintenanceCount = vehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.MAINTENANCE)
                .count();

        if (maintenanceCount > 0) {
            alerts.add(AlertSummary.builder()
                    .type("INFO")
                    .message("Vehicles in maintenance")
                    .count((int) maintenanceCount)
                    .priority("MEDIUM")
                    .build());
        }

        // Count offline vehicles
        long offlineCount = vehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.INACTIVE)
                .count();

        if (offlineCount > 0) {
            alerts.add(AlertSummary.builder()
                    .type("CRITICAL")
                    .message("Vehicles offline")
                    .count((int) offlineCount)
                    .priority("HIGH")
                    .build());
        }

        return alerts;
    }

    private EsgQuickStats computeEsgStats(List<Vehicle> vehicles) {
        long evCount = vehicles.stream()
                .filter(v -> v.getFuelType() == FuelType.EV)
                .count();
        long hybridCount = vehicles.stream()
                .filter(v -> v.getFuelType() == FuelType.HYBRID)
                .count();
        int total = vehicles.size();

        BigDecimal electrification = total > 0
                ? BigDecimal.valueOf((evCount + hybridCount * 0.5) * 100 / total).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Estimate carbon savings (would come from ESG service in real implementation)
        BigDecimal carbonSavings = BigDecimal.valueOf(evCount * 15); // ~15 kg CO2 saved per EV per day

        return EsgQuickStats.builder()
                .carbonSavingsKgToday(carbonSavings)
                .electrificationPercent(electrification)
                .treesEquivalent(carbonSavings.divide(new BigDecimal("22"), 0, RoundingMode.HALF_UP).intValue())
                .build();
    }

    private ChargingStatus computeChargingStatus(Long companyId) {
        // This would come from charging infrastructure service
        // For now, return placeholder data
        return ChargingStatus.builder()
                .totalStations(10)
                .stationsAvailable(6)
                .stationsInUse(3)
                .stationsOffline(1)
                .build();
    }

    private List<VehicleSummary> getTopPerformers(List<Vehicle> vehicles) {
        // Get top 5 vehicles by some metric (placeholder logic)
        List<VehicleSummary> result = new ArrayList<>();
        vehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.ACTIVE)
                .limit(5)
                .forEach(v -> result.add(VehicleSummary.builder()
                        .vehicleId(v.getId())
                        .vehicleNumber(v.getVehicleNumber())
                        .status(v.getStatus() != null ? v.getStatus().name() : "UNKNOWN")
                        .batteryLevel(v.getCurrentBatterySoc() != null ? BigDecimal.valueOf(v.getCurrentBatterySoc()) : null)
                        .todayDistanceKm(new BigDecimal("45.5")) // Placeholder
                        .build()));
        return result;
    }

    private List<VehicleSummary> getVehiclesNeedingAttention(List<Vehicle> vehicles) {
        List<VehicleSummary> needsAttention = new ArrayList<>();

        // Low battery vehicles
        vehicles.stream()
                .filter(v -> v.getFuelType() == FuelType.EV || v.getFuelType() == FuelType.HYBRID)
                .filter(v -> v.getCurrentBatterySoc() != null && v.getCurrentBatterySoc() < BATTERY_LOW.doubleValue())
                .limit(3)
                .forEach(v -> {
                    BigDecimal batteryLevel = BigDecimal.valueOf(v.getCurrentBatterySoc());
                    needsAttention.add(VehicleSummary.builder()
                        .vehicleId(v.getId())
                        .vehicleNumber(v.getVehicleNumber())
                        .status("LOW_BATTERY")
                        .batteryLevel(batteryLevel)
                        .issue("Low battery: " + batteryLevel + "%")
                        .build());
                });

        // Maintenance vehicles
        vehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.MAINTENANCE)
                .limit(2)
                .forEach(v -> needsAttention.add(VehicleSummary.builder()
                        .vehicleId(v.getId())
                        .vehicleNumber(v.getVehicleNumber())
                        .status("MAINTENANCE")
                        .issue("In maintenance")
                        .build()));

        return needsAttention;
    }

    private VehiclePosition mapToPosition(Vehicle vehicle) {
        LocalDateTime now = LocalDateTime.now();
        
        // Use actual coordinates or generate simulated position
        BigDecimal lat = vehicle.getLatitude() != null 
                ? BigDecimal.valueOf(vehicle.getLatitude())
                : new BigDecimal("19.0760").add(BigDecimal.valueOf(Math.random() * 0.1 - 0.05));
        BigDecimal lng = vehicle.getLongitude() != null
                ? BigDecimal.valueOf(vehicle.getLongitude())
                : new BigDecimal("72.8777").add(BigDecimal.valueOf(Math.random() * 0.1 - 0.05));

        Random rand = new Random(vehicle.getId());
        String statusStr = vehicle.getStatus() != null ? vehicle.getStatus().name() : "OFFLINE";
        String batteryStatus = "NORMAL";
        BigDecimal batteryLevel = vehicle.getCurrentBatterySoc() != null 
                ? BigDecimal.valueOf(vehicle.getCurrentBatterySoc()) 
                : null;
        
        if (batteryLevel != null) {
            if (batteryLevel.compareTo(BATTERY_CRITICAL) < 0) batteryStatus = "CRITICAL";
            else if (batteryLevel.compareTo(BATTERY_LOW) < 0) batteryStatus = "LOW";
            else if (batteryLevel.compareTo(BATTERY_FULL) >= 0) batteryStatus = "FULL";
        }

        return VehiclePosition.builder()
                .vehicleId(vehicle.getId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .licensePlate(vehicle.getLicensePlate())
                .latitude(lat)
                .longitude(lng)
                .heading(BigDecimal.valueOf(rand.nextInt(360)))
                .speedKmh(vehicle.getStatus() == Vehicle.VehicleStatus.ACTIVE 
                        ? BigDecimal.valueOf(30 + rand.nextInt(40)) 
                        : BigDecimal.ZERO)
                .status(statusStr)
                .statusColor(getStatusColor(vehicle.getStatus()))
                .batteryPercent(batteryLevel)
                .batteryStatus(batteryStatus)
                .lastUpdateTime(now.minusMinutes(rand.nextInt(5)))
                .secondsSinceUpdate(rand.nextInt(300))
                .fuelType(vehicle.getFuelType() != null ? vehicle.getFuelType().name() : "EV")
                .build();
    }

    private List<VehicleCluster> computeClusters(List<VehiclePosition> positions) {
        // Simple clustering by rounding coordinates
        Map<String, List<VehiclePosition>> clustered = positions.stream()
                .collect(Collectors.groupingBy(p -> 
                        p.getLatitude().setScale(1, RoundingMode.HALF_UP) + "," +
                        p.getLongitude().setScale(1, RoundingMode.HALF_UP)));

        return clustered.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> {
                    String[] coords = e.getKey().split(",");
                    return VehicleCluster.builder()
                            .latitude(new BigDecimal(coords[0]))
                            .longitude(new BigDecimal(coords[1]))
                            .vehicleCount(e.getValue().size())
                            .areaName("Cluster " + e.getKey())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String getStatusColor(Vehicle.VehicleStatus status) {
        if (status == null) return "#808080"; // Gray for unknown
        switch (status) {
            case ACTIVE: return "#4CAF50";     // Green
            case CHARGING: return "#2196F3";   // Blue
            case IN_TRIP: return "#FF9800";    // Orange
            case MAINTENANCE: return "#F44336"; // Red
            case INACTIVE:
            default: return "#808080";          // Gray
        }
    }

    private BigDecimal sumDistance(List<FleetSummary> summaries) {
        return summaries.stream()
                .filter(s -> s.getTotalDistance() != null)
                .map(FleetSummary::getTotalDistance)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumCost(List<FleetSummary> summaries) {
        return summaries.stream()
                .filter(s -> s.getTotalCost() != null)
                .map(FleetSummary::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateChangePercent(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(previous, 1, RoundingMode.HALF_UP);
    }

    private String generateEtag(Long companyId, LocalDateTime timestamp) {
        try {
            String data = companyId + "-" + timestamp.truncatedTo(ChronoUnit.MINUTES);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return "\"" + sb.substring(0, 16) + "\"";
        } catch (Exception e) {
            return "\"" + System.currentTimeMillis() + "\"";
        }
    }

    // ========== CACHE WRAPPER CLASSES ==========

    private static class CachedMetrics {
        final DashboardMetricsResponse metrics;
        final String etag;
        final long cachedAt;

        CachedMetrics(DashboardMetricsResponse metrics, String etag) {
            this.metrics = metrics;
            this.etag = etag;
            this.cachedAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > METRICS_CACHE_TTL_MS;
        }
    }

    private static class CachedPositions {
        final LiveVehiclePositionResponse positions;
        final long cachedAt;

        CachedPositions(LiveVehiclePositionResponse positions) {
            this.positions = positions;
            this.cachedAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > POSITIONS_CACHE_TTL_MS;
        }
    }
}
