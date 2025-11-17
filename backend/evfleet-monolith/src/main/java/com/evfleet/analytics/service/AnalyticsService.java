package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.FleetSummaryResponse;
import com.evfleet.analytics.model.FleetSummary;
import com.evfleet.analytics.repository.FleetSummaryRepository;
import com.evfleet.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
