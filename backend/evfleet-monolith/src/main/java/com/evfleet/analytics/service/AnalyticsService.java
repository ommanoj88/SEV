package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.*;
import com.evfleet.analytics.model.FleetSummary;
import com.evfleet.analytics.repository.FleetSummaryRepository;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.BatteryHealth;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.BatteryHealthRepository;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.maintenance.repository.MaintenanceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics Service
 * Handles all analytics-related business logic
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AnalyticsService {

    private final FleetSummaryRepository fleetSummaryRepository;
    private final VehicleRepository vehicleRepository;
    private final BatteryHealthRepository batteryHealthRepository;
    private final TripRepository tripRepository;
    private final ChargingSessionRepository chargingSessionRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    @Transactional(readOnly = true)
    public FleetSummaryResponse getFleetSummary(Long companyId, LocalDate date) {
        log.info("GET /api/v1/analytics/fleet-summary - companyId: {}, date: {}", companyId, date);

        FleetSummary summary = fleetSummaryRepository.findByCompanyIdAndSummaryDate(companyId, date)
                .orElse(createDefaultSummary(companyId, date));

        return FleetSummaryResponse.fromEntity(summary);
    }

    @Transactional(readOnly = true)
    public FleetSummaryResponse getTodaysSummary(Long companyId) {
        log.info("GET /api/v1/analytics/fleet-summary/today - companyId: {}", companyId);
        return getFleetSummary(companyId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<FleetSummaryResponse> getFleetSummaryRange(Long companyId, LocalDate startDate, LocalDate endDate) {
        log.info("GET /api/v1/analytics/fleet-summary/range - companyId: {}, startDate: {}, endDate: {}",
                companyId, startDate, endDate);

        List<FleetSummary> summaries = fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(
                companyId, startDate, endDate);

        return summaries.stream()
                .map(FleetSummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FleetSummaryResponse> getMonthlyReport(Long companyId, int year, int month) {
        log.info("GET /api/v1/analytics/monthly-report - companyId: {}, year: {}, month: {}", companyId, year, month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return getFleetSummaryRange(companyId, startDate, endDate);
    }

    public FleetSummaryResponse updateFleetSummary(Long companyId, LocalDate date,
                                                     Integer totalVehicles, Integer activeVehicles,
                                                     Integer totalTrips, Double totalDistance,
                                                     BigDecimal totalEnergyConsumed, BigDecimal totalCost) {
        log.info("Updating fleet summary for company: {}, date: {}", companyId, date);

        FleetSummary summary = fleetSummaryRepository.findByCompanyIdAndSummaryDate(companyId, date)
                .orElse(FleetSummary.builder()
                        .companyId(companyId)
                        .summaryDate(date)
                        .totalVehicles(0)
                        .activeVehicles(0)
                        .totalTrips(0)
                        .totalDistance(0.0)
                        .totalEnergyConsumed(BigDecimal.ZERO)
                        .totalCost(BigDecimal.ZERO)
                        .maintenanceCost(BigDecimal.ZERO)
                        .fuelCost(BigDecimal.ZERO)
                        .energyCost(BigDecimal.ZERO)
                        .build());

        if (totalVehicles != null) {
            summary.setTotalVehicles(totalVehicles);
        }
        if (activeVehicles != null) {
            summary.setActiveVehicles(activeVehicles);
        }
        if (totalTrips != null) {
            summary.setTotalTrips(summary.getTotalTrips() + totalTrips);
        }
        if (totalDistance != null) {
            summary.setTotalDistance(summary.getTotalDistance() + totalDistance);
        }
        if (totalEnergyConsumed != null) {
            summary.setTotalEnergyConsumed(summary.getTotalEnergyConsumed().add(totalEnergyConsumed));
        }
        if (totalCost != null) {
            summary.setTotalCost(summary.getTotalCost().add(totalCost));
        }

        FleetSummary saved = fleetSummaryRepository.save(summary);
        log.info("Fleet summary updated successfully");
        return FleetSummaryResponse.fromEntity(saved);
    }

    /**
     * Update maintenance cost in fleet summary
     */
    public FleetSummaryResponse updateMaintenanceCost(Long companyId, LocalDate date, BigDecimal maintenanceCost) {
        log.info("Updating maintenance cost for company: {}, date: {}, cost: {}", companyId, date, maintenanceCost);

        FleetSummary summary = fleetSummaryRepository.findByCompanyIdAndSummaryDate(companyId, date)
                .orElse(FleetSummary.builder()
                        .companyId(companyId)
                        .summaryDate(date)
                        .totalVehicles(0)
                        .activeVehicles(0)
                        .totalTrips(0)
                        .totalDistance(0.0)
                        .totalEnergyConsumed(BigDecimal.ZERO)
                        .totalCost(BigDecimal.ZERO)
                        .maintenanceCost(BigDecimal.ZERO)
                        .fuelCost(BigDecimal.ZERO)
                        .energyCost(BigDecimal.ZERO)
                        .build());

        if (maintenanceCost != null) {
            summary.setMaintenanceCost(summary.getMaintenanceCost().add(maintenanceCost));
            summary.setTotalCost(summary.getTotalCost().add(maintenanceCost));
        }

        FleetSummary saved = fleetSummaryRepository.save(summary);
        log.info("Maintenance cost updated successfully");
        return FleetSummaryResponse.fromEntity(saved);
    }

    /**
     * Get comprehensive fleet analytics including vehicle status breakdown, battery metrics, and utilization
     * E1 Fix: Provides complete analytics data matching frontend expectations
     */
    @Transactional(readOnly = true)
    public FleetAnalyticsResponse getFleetAnalytics(Long companyId) {
        log.info("GET fleet analytics - companyId: {}", companyId);

        // Get all vehicles for the company
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);

        // Count vehicles by status
        int totalVehicles = vehicles.size();
        int activeVehicles = (int) vehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.ACTIVE)
                .count();
        int inactiveVehicles = (int) vehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.INACTIVE)
                .count();
        int chargingVehicles = (int) vehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.CHARGING)
                .count();
        int maintenanceVehicles = (int) vehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.MAINTENANCE)
                .count();
        int inTripVehicles = (int) vehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.IN_TRIP)
                .count();

        // Calculate battery metrics (only for vehicles with battery health data)
        List<Long> vehicleIds = vehicles.stream().map(Vehicle::getId).collect(Collectors.toList());
        List<BatteryHealth> latestBatteryHealth = new ArrayList<>();
        for (Long vehicleId : vehicleIds) {
            batteryHealthRepository.findFirstByVehicleIdOrderByRecordedAtDesc(vehicleId)
                    .ifPresent(latestBatteryHealth::add);
        }

        Double averageBatteryLevel = latestBatteryHealth.isEmpty() ? 0.0 :
                latestBatteryHealth.stream()
                        .mapToDouble(BatteryHealth::getSoc)
                        .average()
                        .orElse(0.0);

        Double averageBatteryHealth = latestBatteryHealth.isEmpty() ? 0.0 :
                latestBatteryHealth.stream()
                        .mapToDouble(BatteryHealth::getSoh)
                        .average()
                        .orElse(0.0);

        // Get trip metrics for the last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Trip> recentTrips = tripRepository.findByCompanyAndDateRange(
                companyId, thirtyDaysAgo, LocalDateTime.now());

        long totalTrips = recentTrips.size();
        double totalDistance = recentTrips.stream()
                .mapToDouble(trip -> trip.getDistance() != null ? trip.getDistance() : 0.0)
                .sum();
        double totalEnergyConsumed = recentTrips.stream()
                .mapToDouble(trip -> trip.getEnergyConsumed() != null ? trip.getEnergyConsumed() : 0.0)
                .sum();

        // Calculate utilization metrics
        double totalHoursInPeriod = 30 * 24.0;  // 30 days
        double totalActiveHours = recentTrips.stream()
                .mapToDouble(trip -> {
                    if (trip.getStartTime() != null && trip.getEndTime() != null) {
                        return ChronoUnit.MINUTES.between(trip.getStartTime(), trip.getEndTime()) / 60.0;
                    }
                    return 0.0;
                })
                .sum();

        double utilizationRate = totalVehicles > 0 ?
                (totalActiveHours / (totalHoursInPeriod * totalVehicles)) * 100 : 0.0;
        double averageUtilization = totalVehicles > 0 ?
                totalActiveHours / (30.0 * totalVehicles) : 0.0;

        // Build summary
        FleetAnalyticsResponse.SummaryData summary = FleetAnalyticsResponse.SummaryData.builder()
                .totalVehicles(totalVehicles)
                .totalTrips(totalTrips)
                .totalDistance(totalDistance)
                .averageUtilization(averageUtilization)
                .build();

        return FleetAnalyticsResponse.builder()
                .totalVehicles(totalVehicles)
                .activeVehicles(activeVehicles)
                .inactiveVehicles(inactiveVehicles)
                .chargingVehicles(chargingVehicles)
                .maintenanceVehicles(maintenanceVehicles)
                .inTripVehicles(inTripVehicles)
                .averageBatteryLevel(averageBatteryLevel)
                .averageBatteryHealth(averageBatteryHealth)
                .totalTrips(totalTrips)
                .totalDistance(totalDistance)
                .totalEnergyConsumed(totalEnergyConsumed)
                .utilizationRate(utilizationRate)
                .averageUtilization(averageUtilization)
                .summary(summary)
                .build();
    }

    /**
     * Get utilization reports for all vehicles
     * E2 Fix: Implements missing utilization reports endpoint
     */
    @Transactional(readOnly = true)
    public List<VehicleUtilizationResponse> getUtilizationReports(
            Long companyId, LocalDate startDate, LocalDate endDate) {
        log.info("GET utilization reports - companyId: {}, startDate: {}, endDate: {}",
                companyId, startDate, endDate);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<VehicleUtilizationResponse> reports = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            // Get trips for this vehicle in the date range
            List<Trip> trips = tripRepository.findByCompanyAndDateRange(
                    companyId, startDateTime, endDateTime).stream()
                    .filter(t -> t.getVehicleId().equals(vehicle.getId()))
                    .collect(Collectors.toList());

            // Calculate metrics
            int tripCount = trips.size();
            double totalDistance = trips.stream()
                    .mapToDouble(t -> t.getDistance() != null ? t.getDistance() : 0.0)
                    .sum();
            double totalEnergy = trips.stream()
                    .mapToDouble(t -> t.getEnergyConsumed() != null ? t.getEnergyConsumed() : 0.0)
                    .sum();

            double activeHours = trips.stream()
                    .mapToDouble(trip -> {
                        if (trip.getStartTime() != null && trip.getEndTime() != null) {
                            return ChronoUnit.MINUTES.between(trip.getStartTime(), trip.getEndTime()) / 60.0;
                        }
                        return 0.0;
                    })
                    .sum();

            // Calculate utilization rate
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            double totalAvailableHours = daysBetween * 24.0;
            double utilizationRate = (activeHours / totalAvailableHours) * 100;

            // Calculate efficiency (km/kWh or km/L)
            double efficiency = totalEnergy > 0 ? totalDistance / totalEnergy : 0.0;

            // Determine status
            String status;
            if (utilizationRate >= 75) {
                status = "optimal";
            } else if (utilizationRate >= 50) {
                status = "underutilized";
            } else {
                status = "severely-underutilized";
            }

            String vehicleName = vehicle.getMake() + " " + vehicle.getModel();

            reports.add(VehicleUtilizationResponse.builder()
                    .vehicleId(vehicle.getId())
                    .vehicleName(vehicleName)
                    .vehicleNumber(vehicle.getVehicleNumber())
                    .utilizationRate(utilizationRate)
                    .activeHours(activeHours)
                    .trips(tripCount)
                    .distance(totalDistance)
                    .efficiency(efficiency)
                    .status(status)
                    .build());
        }

        // Sort by utilization rate descending
        reports.sort((a, b) -> Double.compare(b.getUtilizationRate(), a.getUtilizationRate()));

        return reports;
    }

    /**
     * Get cost analytics for company
     * E3 Fix: Implements missing cost analytics endpoint
     */
    @Transactional(readOnly = true)
    public List<CostAnalyticsResponse> getCostAnalytics(
            Long companyId, LocalDate startDate, LocalDate endDate) {
        log.info("GET cost analytics - companyId: {}, startDate: {}, endDate: {}",
                companyId, startDate, endDate);

        List<FleetSummary> summaries = fleetSummaryRepository
                .findByCompanyIdAndSummaryDateBetween(companyId, startDate, endDate);

        if (summaries.isEmpty()) {
            return Collections.emptyList();
        }

        // Aggregate by month
        Map<String, List<FleetSummary>> byMonth = summaries.stream()
                .collect(Collectors.groupingBy(s ->
                        s.getSummaryDate().getYear() + "-" +
                                String.format("%02d", s.getSummaryDate().getMonthValue())));

        List<CostAnalyticsResponse> analytics = new ArrayList<>();

        for (Map.Entry<String, List<FleetSummary>> entry : byMonth.entrySet()) {
            List<FleetSummary> monthSummaries = entry.getValue();

            BigDecimal energyCost = monthSummaries.stream()
                    .map(s -> s.getEnergyCost() != null ? s.getEnergyCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal maintenanceCost = monthSummaries.stream()
                    .map(s -> s.getMaintenanceCost() != null ? s.getMaintenanceCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal fuelCost = monthSummaries.stream()
                    .map(s -> s.getFuelCost() != null ? s.getFuelCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCost = monthSummaries.stream()
                    .map(s -> s.getTotalCost() != null ? s.getTotalCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Double totalDistance = monthSummaries.stream()
                    .mapToDouble(s -> s.getTotalDistance() != null ? s.getTotalDistance() : 0.0)
                    .sum();

            Integer avgVehicles = (int) monthSummaries.stream()
                    .mapToInt(s -> s.getTotalVehicles() != null ? s.getTotalVehicles() : 0)
                    .average()
                    .orElse(0);

            Double costPerKm = totalDistance > 0 ?
                    totalCost.divide(BigDecimal.valueOf(totalDistance), 4, RoundingMode.HALF_UP).doubleValue() : 0.0;

            Double costPerVehicle = avgVehicles > 0 ?
                    totalCost.divide(BigDecimal.valueOf(avgVehicles), 2, RoundingMode.HALF_UP).doubleValue() : 0.0;

            String period = entry.getKey();

            analytics.add(CostAnalyticsResponse.builder()
                    .period(period)
                    .energyCost(energyCost.add(fuelCost))  // Combine energy sources
                    .maintenanceCost(maintenanceCost)
                    .insuranceCost(BigDecimal.ZERO)  // Not tracked yet
                    .otherCosts(BigDecimal.ZERO)  // Not tracked yet
                    .totalCost(totalCost)
                    .costPerKm(costPerKm)
                    .costPerVehicle(costPerVehicle)
                    .vehicleCount(avgVehicles)
                    .totalDistance(totalDistance)
                    .build());
        }

        analytics.sort((a, b) -> b.getPeriod().compareTo(a.getPeriod()));

        return analytics;
    }

    /**
     * Get TCO analysis for a specific vehicle
     * E3 Fix: Implements missing TCO analysis endpoint
     */
    @Transactional(readOnly = true)
    public TCOAnalysisResponse getTCOAnalysis(Long vehicleId) {
        log.info("GET TCO analysis - vehicleId: {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        String vehicleName = vehicle.getMake() + " " + vehicle.getModel();

        // Note: These fields may not exist in Vehicle entity yet
        // Using placeholder values - should be updated when database schema is enhanced
        BigDecimal purchasePrice = BigDecimal.ZERO;  // TODO: Add to Vehicle entity
        Integer ageMonths = 0;  // TODO: Calculate from purchase_date
        BigDecimal depreciation = BigDecimal.ZERO;  // TODO: Calculate based on age

        // Get all trips for this vehicle
        List<Trip> allTrips = tripRepository.findByVehicleId(vehicleId);
        double totalDistance = allTrips.stream()
                .mapToDouble(t -> t.getDistance() != null ? t.getDistance() : 0.0)
                .sum();
        double totalEnergy = allTrips.stream()
                .mapToDouble(t -> t.getEnergyConsumed() != null ? t.getEnergyConsumed() : 0.0)
                .sum();

        // Estimate energy costs (this is a simplified calculation)
        // TODO: Get actual costs from charging_sessions table
        BigDecimal energyCosts = BigDecimal.valueOf(totalEnergy * 0.12);  // Assume $0.12/kWh

        // Get maintenance costs
        // TODO: Sum from maintenance_records table
        BigDecimal maintenanceCosts = BigDecimal.ZERO;

        BigDecimal totalCost = purchasePrice.add(depreciation).add(energyCosts).add(maintenanceCosts);

        Double costPerKm = totalDistance > 0 ?
                totalCost.divide(BigDecimal.valueOf(totalDistance), 4, RoundingMode.HALF_UP).doubleValue() : 0.0;

        Double costPerYear = ageMonths > 0 ?
                totalCost.divide(BigDecimal.valueOf(ageMonths / 12.0), 2, RoundingMode.HALF_UP).doubleValue() : 0.0;

        return TCOAnalysisResponse.builder()
                .vehicleId(vehicleId)
                .vehicleName(vehicleName)
                .vehicleNumber(vehicle.getVehicleNumber())
                .fuelType(vehicle.getFuelType())
                .purchasePrice(purchasePrice)
                .depreciation(depreciation)
                .ageMonths(ageMonths)
                .energyCosts(energyCosts)
                .maintenanceCosts(maintenanceCosts)
                .insuranceCosts(BigDecimal.ZERO)
                .taxesFees(BigDecimal.ZERO)
                .otherCosts(BigDecimal.ZERO)
                .totalCost(totalCost)
                .costPerKm(costPerKm)
                .costPerYear(costPerYear)
                .totalDistance(totalDistance)
                .comparisonWithICE(null)  // TODO: Implement for EVs
                .build();
    }

    private FleetSummary createDefaultSummary(Long companyId, LocalDate date) {
        return FleetSummary.builder()
                .companyId(companyId)
                .summaryDate(date)
                .totalVehicles(0)
                .activeVehicles(0)
                .totalTrips(0)
                .totalDistance(0.0)
                .totalEnergyConsumed(BigDecimal.ZERO)
                .totalCost(BigDecimal.ZERO)
                .build();
    }
}
