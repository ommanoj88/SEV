package com.evfleet.charging.service;

import com.evfleet.charging.dto.CostSummaryResponse;
import com.evfleet.charging.dto.StationAnalyticsResponse;
import com.evfleet.charging.dto.UtilizationMetricsResponse;
import com.evfleet.charging.entity.ChargingSession;
import com.evfleet.charging.entity.ChargingStation;
import com.evfleet.charging.exception.ResourceNotFoundException;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.charging.repository.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ChargingAnalyticsService
 * Provides comprehensive analytics for charging operations
 * 
 * @since PR-10 (Charging Analytics)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChargingAnalyticsServiceImpl implements ChargingAnalyticsService {

    private final ChargingSessionRepository sessionRepository;
    private final ChargingStationRepository stationRepository;

    @Override
    public StationAnalyticsResponse getStationAnalytics(Long stationId) {
        log.info("Getting analytics for station ID: {}", stationId);
        
        // Use all-time data
        LocalDateTime startDate = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.now();
        
        return getStationAnalytics(stationId, startDate, endDate);
    }

    @Override
    public StationAnalyticsResponse getStationAnalytics(Long stationId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting analytics for station ID: {} from {} to {}", stationId, startDate, endDate);
        
        // Get station details
        ChargingStation station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResourceNotFoundException("Charging station not found with ID: " + stationId));
        
        // Get all sessions for the station in the date range
        List<ChargingSession> allSessions = sessionRepository.findByStationIdAndDateRange(stationId, startDate, endDate);
        
        // Get active sessions
        Long activeSessions = sessionRepository.countActiveSessionsByStation(stationId);
        
        // Filter completed sessions
        List<ChargingSession> completedSessions = allSessions.stream()
            .filter(s -> s.getStatus() == ChargingSession.SessionStatus.COMPLETED)
            .collect(Collectors.toList());
        
        // Calculate metrics
        BigDecimal totalEnergy = sessionRepository.getTotalEnergyByStation(stationId, startDate, endDate);
        BigDecimal totalCost = sessionRepository.getTotalCostByStation(stationId, startDate, endDate);
        Double avgDuration = sessionRepository.getAverageDurationByStation(stationId, startDate, endDate);
        
        // Calculate average energy per session
        BigDecimal avgEnergyPerSession = BigDecimal.ZERO;
        if (!completedSessions.isEmpty()) {
            avgEnergyPerSession = totalEnergy.divide(
                BigDecimal.valueOf(completedSessions.size()), 
                2, 
                RoundingMode.HALF_UP
            );
        }
        
        // Calculate average session cost
        BigDecimal avgSessionCost = BigDecimal.ZERO;
        if (!completedSessions.isEmpty()) {
            avgSessionCost = totalCost.divide(
                BigDecimal.valueOf(completedSessions.size()), 
                2, 
                RoundingMode.HALF_UP
            );
        }
        
        // Calculate total charging minutes
        Long totalChargingMinutes = completedSessions.stream()
            .filter(s -> s.getDurationMinutes() != null)
            .mapToLong(ChargingSession::getDurationMinutes)
            .sum();
        
        // Calculate utilization rate
        // Utilization = (Total charging time / (Total slots * Time period)) * 100
        long periodMinutes = java.time.Duration.between(startDate, endDate).toMinutes();
        long totalAvailableMinutes = station.getTotalSlots() * periodMinutes;
        
        BigDecimal utilizationRate = BigDecimal.ZERO;
        if (totalAvailableMinutes > 0) {
            utilizationRate = BigDecimal.valueOf(totalChargingMinutes)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalAvailableMinutes), 2, RoundingMode.HALF_UP);
        }
        
        return StationAnalyticsResponse.builder()
            .stationId(stationId)
            .stationName(station.getName())
            .totalSlots(station.getTotalSlots())
            .availableSlots(station.getAvailableSlots())
            .totalSessions((long) allSessions.size())
            .completedSessions((long) completedSessions.size())
            .activeSessions(activeSessions)
            .utilizationRate(utilizationRate)
            .totalEnergyCharged(totalEnergy)
            .averageEnergyPerSession(avgEnergyPerSession)
            .totalRevenue(totalCost)
            .averageSessionCost(avgSessionCost)
            .pricePerKwh(station.getPricePerKwh())
            .averageSessionDurationMinutes(avgDuration.longValue())
            .totalChargingMinutes(totalChargingMinutes)
            .build();
    }

    @Override
    public UtilizationMetricsResponse getUtilizationMetrics() {
        log.info("Getting overall utilization metrics");
        
        // Get all stations
        List<ChargingStation> allStations = stationRepository.findAll();
        List<ChargingStation> activeStations = allStations.stream()
            .filter(s -> s.getStatus() == ChargingStation.StationStatus.ACTIVE)
            .collect(Collectors.toList());
        
        // Calculate slot statistics
        int totalSlots = allStations.stream()
            .mapToInt(ChargingStation::getTotalSlots)
            .sum();
        
        int availableSlots = allStations.stream()
            .mapToInt(ChargingStation::getAvailableSlots)
            .sum();
        
        int occupiedSlots = totalSlots - availableSlots;
        
        // Calculate overall utilization rate
        BigDecimal overallUtilizationRate = BigDecimal.ZERO;
        if (totalSlots > 0) {
            overallUtilizationRate = BigDecimal.valueOf(occupiedSlots)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalSlots), 2, RoundingMode.HALF_UP);
        }
        
        // Get session counts
        Long activeSessions = sessionRepository.countAllActiveSessions();
        
        // Get today's completed sessions
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<ChargingSession> todayCompletedSessions = sessionRepository.findCompletedSessionsInRange(todayStart, todayEnd);
        
        // Get all sessions for average calculation
        List<ChargingSession> allSessions = sessionRepository.findAll();
        Long totalSessions = (long) allSessions.size();
        
        // Calculate average station utilization
        List<BigDecimal> stationUtilizations = new ArrayList<>();
        for (ChargingStation station : allStations) {
            if (station.getTotalSlots() > 0) {
                BigDecimal util = BigDecimal.valueOf(station.getTotalSlots() - station.getAvailableSlots())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(station.getTotalSlots()), 2, RoundingMode.HALF_UP);
                stationUtilizations.add(util);
            }
        }
        
        BigDecimal avgStationUtilization = BigDecimal.ZERO;
        if (!stationUtilizations.isEmpty()) {
            avgStationUtilization = stationUtilizations.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(stationUtilizations.size()), 2, RoundingMode.HALF_UP);
        }
        
        // Get top stations by utilization
        List<UtilizationMetricsResponse.StationUtilizationSummary> topStations = allStations.stream()
            .map(station -> {
                Long stationSessions = sessionRepository.countCompletedSessionsByStation(station.getId());
                BigDecimal util = BigDecimal.ZERO;
                if (station.getTotalSlots() > 0) {
                    util = BigDecimal.valueOf(station.getTotalSlots() - station.getAvailableSlots())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(station.getTotalSlots()), 2, RoundingMode.HALF_UP);
                }
                return UtilizationMetricsResponse.StationUtilizationSummary.builder()
                    .stationId(station.getId())
                    .stationName(station.getName())
                    .utilizationRate(util)
                    .sessionsCount(stationSessions)
                    .build();
            })
            .sorted(Comparator.comparing(UtilizationMetricsResponse.StationUtilizationSummary::getUtilizationRate).reversed())
            .limit(5)
            .collect(Collectors.toList());
        
        return UtilizationMetricsResponse.builder()
            .totalStations(allStations.size())
            .activeStations(activeStations.size())
            .totalSlots(totalSlots)
            .availableSlots(availableSlots)
            .occupiedSlots(occupiedSlots)
            .overallUtilizationRate(overallUtilizationRate)
            .averageStationUtilization(avgStationUtilization)
            .totalSessions(totalSessions)
            .activeSessions(activeSessions)
            .completedSessionsToday((long) todayCompletedSessions.size())
            .topStations(topStations)
            .build();
    }

    @Override
    public CostSummaryResponse getCostSummary(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting cost summary from {} to {}", startDate, endDate);
        
        // Get all completed sessions in the range
        List<ChargingSession> completedSessions = sessionRepository.findCompletedSessionsInRange(startDate, endDate);
        
        // Calculate revenue
        BigDecimal totalRevenue = sessionRepository.getTotalRevenue(startDate, endDate);
        
        // Calculate total energy
        BigDecimal totalEnergy = sessionRepository.getTotalEnergyCharged(startDate, endDate);
        
        // Calculate averages
        BigDecimal avgSessionCost = BigDecimal.ZERO;
        BigDecimal avgEnergyPerSession = BigDecimal.ZERO;
        
        if (!completedSessions.isEmpty()) {
            avgSessionCost = totalRevenue.divide(
                BigDecimal.valueOf(completedSessions.size()), 
                2, 
                RoundingMode.HALF_UP
            );
            
            avgEnergyPerSession = totalEnergy.divide(
                BigDecimal.valueOf(completedSessions.size()), 
                2, 
                RoundingMode.HALF_UP
            );
        }
        
        // Calculate min, max, median cost
        List<BigDecimal> costs = completedSessions.stream()
            .map(ChargingSession::getCost)
            .filter(cost -> cost != null)
            .sorted()
            .collect(Collectors.toList());
        
        BigDecimal minCost = costs.isEmpty() ? BigDecimal.ZERO : costs.get(0);
        BigDecimal maxCost = costs.isEmpty() ? BigDecimal.ZERO : costs.get(costs.size() - 1);
        BigDecimal medianCost = BigDecimal.ZERO;
        
        if (!costs.isEmpty()) {
            int middle = costs.size() / 2;
            if (costs.size() % 2 == 0) {
                medianCost = costs.get(middle - 1).add(costs.get(middle))
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            } else {
                medianCost = costs.get(middle);
            }
        }
        
        // Calculate revenue per kWh
        BigDecimal revenuePerKwh = BigDecimal.ZERO;
        if (totalEnergy.compareTo(BigDecimal.ZERO) > 0) {
            revenuePerKwh = totalRevenue.divide(totalEnergy, 2, RoundingMode.HALF_UP);
        }
        
        // Calculate average price per kWh from all stations
        List<ChargingStation> stations = stationRepository.findAll();
        BigDecimal avgPricePerKwh = BigDecimal.ZERO;
        if (!stations.isEmpty()) {
            avgPricePerKwh = stations.stream()
                .map(ChargingStation::getPricePerKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(stations.size()), 2, RoundingMode.HALF_UP);
        }
        
        // Get all sessions count (not just completed)
        List<ChargingSession> allSessions = sessionRepository.findAll().stream()
            .filter(s -> s.getStartTime().isAfter(startDate) && s.getStartTime().isBefore(endDate))
            .collect(Collectors.toList());
        
        return CostSummaryResponse.builder()
            .periodStart(startDate)
            .periodEnd(endDate)
            .totalRevenue(totalRevenue)
            .averageSessionCost(avgSessionCost)
            .totalEnergyCharged(totalEnergy)
            .minSessionCost(minCost)
            .maxSessionCost(maxCost)
            .medianSessionCost(medianCost)
            .averageEnergyPerSession(avgEnergyPerSession)
            .totalSessions(BigDecimal.valueOf(allSessions.size()))
            .completedSessions(BigDecimal.valueOf(completedSessions.size()))
            .averagePricePerKwh(avgPricePerKwh)
            .revenuePerKwh(revenuePerKwh)
            .build();
    }

    @Override
    public CostSummaryResponse getTodayCostSummary() {
        log.info("Getting today's cost summary");
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return getCostSummary(todayStart, todayEnd);
    }

    @Override
    public CostSummaryResponse getMonthCostSummary() {
        log.info("Getting current month's cost summary");
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        return getCostSummary(monthStart, monthEnd);
    }
}
