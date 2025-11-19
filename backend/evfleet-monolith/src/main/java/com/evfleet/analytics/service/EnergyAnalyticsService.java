package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.EnergyConsumptionResponse;
import com.evfleet.analytics.model.EnergyConsumptionAnalytics;
import com.evfleet.analytics.repository.EnergyConsumptionAnalyticsRepository;
import com.evfleet.charging.model.ChargingSession;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Energy Analytics Service
 * Handles energy consumption tracking and efficiency analysis for electric vehicles
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EnergyAnalyticsService {

    private final EnergyConsumptionAnalyticsRepository energyAnalyticsRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final ChargingSessionRepository chargingSessionRepository;

    /**
     * Aggregate daily energy analytics for a vehicle
     */
    @Transactional
    public EnergyConsumptionResponse aggregateDailyEnergyAnalytics(Long vehicleId, LocalDate date) {
        log.info("Aggregating energy analytics for vehicle: {} on date: {}", vehicleId, date);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        // Only process electric vehicles
        if (vehicle.getFuelType() != FuelType.EV) {
            log.warn("Vehicle {} is not electric, skipping energy analytics", vehicleId);
            return null;
        }

        // Get or create analytics for the date
        EnergyConsumptionAnalytics analytics = energyAnalyticsRepository
                .findByVehicleIdAndAnalysisDate(vehicleId, date)
                .orElse(EnergyConsumptionAnalytics.builder()
                        .companyId(vehicle.getCompanyId())
                        .vehicleId(vehicleId)
                        .analysisDate(date)
                        .build());

        // Aggregate data for the day
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // 1. Aggregate trips for the day
        List<Trip> dailyTrips = tripRepository.findByVehicleIdAndStartTimeBetween(
                vehicleId, startOfDay, endOfDay);

        BigDecimal totalDistance = dailyTrips.stream()
                .map(t -> t.getDistance() != null ? BigDecimal.valueOf(t.getDistance()) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEnergyFromTrips = dailyTrips.stream()
                .map(t -> t.getEnergyConsumed() != null ? t.getEnergyConsumed() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        analytics.setTotalDistance(totalDistance);
        analytics.setTotalEnergyConsumed(totalEnergyFromTrips);

        // 2. Aggregate charging sessions for the day
        List<ChargingSession> dailySessions = chargingSessionRepository
                .findByVehicleIdAndStartTimeBetween(vehicleId, startOfDay, endOfDay);

        analytics.setTotalChargingSessions(dailySessions.size());

        BigDecimal totalChargingCost = dailySessions.stream()
                .map(s -> s.getCost() != null ? s.getCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        analytics.setTotalChargingCost(totalChargingCost);

        // 3. Calculate efficiency metrics
        calculateEfficiencyMetrics(analytics, dailyTrips);

        // 4. Calculate all derived metrics
        analytics.calculateAllMetrics();

        // Save analytics
        analytics = energyAnalyticsRepository.save(analytics);
        log.info("Energy analytics saved for vehicle: {} on date: {}", vehicleId, date);

        return convertToResponse(analytics, vehicle);
    }

    /**
     * Get energy consumption for a vehicle in a date range
     */
    @Transactional(readOnly = true)
    public List<EnergyConsumptionResponse> getEnergyConsumption(
            Long vehicleId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting energy consumption for vehicle: {} from {} to {}", vehicleId, startDate, endDate);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        List<EnergyConsumptionAnalytics> analyticsList = energyAnalyticsRepository
                .findByVehicleIdAndAnalysisDateBetween(vehicleId, startDate, endDate);

        return analyticsList.stream()
                .map(a -> convertToResponse(a, vehicle))
                .collect(Collectors.toList());
    }

    /**
     * Get energy consumption for a single date
     */
    @Transactional(readOnly = true)
    public EnergyConsumptionResponse getEnergyConsumptionForDate(Long vehicleId, LocalDate date) {
        log.info("Getting energy consumption for vehicle: {} on date: {}", vehicleId, date);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        Optional<EnergyConsumptionAnalytics> analyticsOpt = energyAnalyticsRepository
                .findByVehicleIdAndAnalysisDate(vehicleId, date);

        if (analyticsOpt.isEmpty()) {
            // Calculate fresh analytics if not found
            return aggregateDailyEnergyAnalytics(vehicleId, date);
        }

        return convertToResponse(analyticsOpt.get(), vehicle);
    }

    /**
     * Compare vehicle efficiency across the fleet
     */
    @Transactional(readOnly = true)
    public List<EnergyConsumptionResponse> compareVehicleEfficiency(
            Long companyId, LocalDate startDate, LocalDate endDate) {
        log.info("Comparing vehicle efficiency for company: {} from {} to {}", companyId, startDate, endDate);

        List<Vehicle> electricVehicles = vehicleRepository.findByCompanyId(companyId).stream()
                .filter(v -> v.getFuelType() == FuelType.EV)
                .collect(Collectors.toList());

        return electricVehicles.stream()
                .map(vehicle -> {
                    // Get aggregated analytics for the period
                    List<EnergyConsumptionAnalytics> analyticsList = energyAnalyticsRepository
                            .findByVehicleIdAndAnalysisDateBetween(vehicle.getId(), startDate, endDate);

                    if (analyticsList.isEmpty()) {
                        return null;
                    }

                    // Aggregate metrics
                    BigDecimal totalEnergy = analyticsList.stream()
                            .map(EnergyConsumptionAnalytics::getTotalEnergyConsumed)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalDistance = analyticsList.stream()
                            .map(EnergyConsumptionAnalytics::getTotalDistance)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalCost = analyticsList.stream()
                            .map(EnergyConsumptionAnalytics::getTotalChargingCost)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    int totalSessions = analyticsList.stream()
                            .mapToInt(EnergyConsumptionAnalytics::getTotalChargingSessions)
                            .sum();

                    // Calculate average efficiency
                    BigDecimal avgEfficiency = BigDecimal.ZERO;
                    if (totalDistance.compareTo(BigDecimal.ZERO) > 0) {
                        avgEfficiency = totalEnergy
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalDistance, 4, RoundingMode.HALF_UP);
                    }

                    String vehicleName = vehicle.getMake() + " " + vehicle.getModel();

                    return EnergyConsumptionResponse.builder()
                            .vehicleId(vehicle.getId())
                            .vehicleName(vehicleName)
                            .energyConsumed(totalEnergy)
                            .distance(totalDistance)
                            .chargingSessions(totalSessions)
                            .efficiency(avgEfficiency)
                            .chargingCost(totalCost)
                            .build();
                })
                .filter(r -> r != null)
                .sorted((a, b) -> a.getEfficiency().compareTo(b.getEfficiency()))
                .collect(Collectors.toList());
    }

    /**
     * Get energy trend for a vehicle
     */
    @Transactional(readOnly = true)
    public List<EnergyConsumptionResponse> getEnergyTrend(
            Long vehicleId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting energy trend for vehicle: {} from {} to {}", vehicleId, startDate, endDate);

        return getEnergyConsumption(vehicleId, startDate, endDate);
    }

    /**
     * Aggregate energy analytics for all vehicles (scheduled job)
     */
    @Transactional
    public void aggregateEnergyAnalyticsForAllVehicles(LocalDate date) {
        log.info("Starting energy analytics aggregation for all vehicles on date: {}", date);

        List<Vehicle> electricVehicles = vehicleRepository.findAll().stream()
                .filter(v -> v.getFuelType() == FuelType.EV)
                .collect(Collectors.toList());

        int count = 0;
        for (Vehicle vehicle : electricVehicles) {
            try {
                aggregateDailyEnergyAnalytics(vehicle.getId(), date);
                count++;
            } catch (Exception e) {
                log.error("Error aggregating energy analytics for vehicle: {}", vehicle.getId(), e);
            }
        }

        log.info("Completed energy analytics aggregation for {} vehicles", count);
    }

    /**
     * Calculate efficiency metrics from trips
     */
    private void calculateEfficiencyMetrics(EnergyConsumptionAnalytics analytics, List<Trip> trips) {
        if (trips.isEmpty()) {
            return;
        }

        // Calculate best and worst efficiency from trips
        BigDecimal bestEfficiency = BigDecimal.valueOf(Double.MAX_VALUE);
        BigDecimal worstEfficiency = BigDecimal.ZERO;

        for (Trip trip : trips) {
            if (trip.getDistance() != null && trip.getDistance() > 0 &&
                    trip.getEnergyConsumed() != null && trip.getEnergyConsumed().compareTo(BigDecimal.ZERO) > 0) {

                // Calculate efficiency for this trip (kWh per 100km)
                BigDecimal tripEfficiency = trip.getEnergyConsumed()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(trip.getDistance()), 4, RoundingMode.HALF_UP);

                if (tripEfficiency.compareTo(bestEfficiency) < 0) {
                    bestEfficiency = tripEfficiency;
                }
                if (tripEfficiency.compareTo(worstEfficiency) > 0) {
                    worstEfficiency = tripEfficiency;
                }
            }
        }

        if (bestEfficiency.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) < 0) {
            analytics.setBestEfficiency(bestEfficiency);
        }
        if (worstEfficiency.compareTo(BigDecimal.ZERO) > 0) {
            analytics.setWorstEfficiency(worstEfficiency);
        }

        // Set regenerative energy (placeholder - would come from trip data if tracked)
        analytics.setRegenerativeEnergy(BigDecimal.ZERO);

        // Set idle energy loss (placeholder - would come from vehicle telemetry)
        analytics.setIdleEnergyLoss(BigDecimal.ZERO);
    }

    /**
     * Convert entity to response DTO
     */
    private EnergyConsumptionResponse convertToResponse(
            EnergyConsumptionAnalytics analytics, Vehicle vehicle) {
        String vehicleName = vehicle.getMake() + " " + vehicle.getModel();

        return EnergyConsumptionResponse.builder()
                .id(analytics.getId())
                .date(analytics.getAnalysisDate().toString())
                .vehicleId(vehicle.getId())
                .vehicleName(vehicleName)
                .energyConsumed(analytics.getTotalEnergyConsumed())
                .distance(analytics.getTotalDistance())
                .chargingSessions(analytics.getTotalChargingSessions())
                .efficiency(analytics.getAverageEfficiency())
                .bestEfficiency(analytics.getBestEfficiency())
                .worstEfficiency(analytics.getWorstEfficiency())
                .chargingCost(analytics.getTotalChargingCost())
                .costPerKwh(analytics.getAverageCostPerKwh())
                .costPerKm(analytics.getCostPerKm())
                .regenEnergy(analytics.getRegenerativeEnergy())
                .regenPercentage(analytics.getRegenPercentage())
                .idleEnergyLoss(analytics.getIdleEnergyLoss())
                .co2Saved(analytics.getCo2Saved())
                .build();
    }
}
