package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.FleetSummaryResponse;
import com.evfleet.analytics.dto.TCOAnalysisResponse;
import com.evfleet.analytics.entity.CostAnalytics;
import com.evfleet.analytics.entity.FleetSummary;
import com.evfleet.analytics.entity.TCOAnalysis;
import com.evfleet.analytics.entity.UtilizationReport;
import com.evfleet.analytics.repository.CostAnalyticsRepository;
import com.evfleet.analytics.repository.FleetSummaryRepository;
import com.evfleet.analytics.repository.TCOAnalysisRepository;
import com.evfleet.analytics.repository.UtilizationReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final FleetSummaryRepository fleetSummaryRepository;
    private final TCOAnalysisRepository tcoAnalysisRepository;
    private final CostAnalyticsRepository costAnalyticsRepository;
    private final UtilizationReportRepository utilizationReportRepository;

    @Override
    public FleetSummaryResponse getFleetSummary(String companyId) {
        FleetSummary fleetSummary = fleetSummaryRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new RuntimeException("Fleet summary not found for company: " + companyId));

        return new FleetSummaryResponse(
                fleetSummary.getCompanyId(),
                fleetSummary.getTotalVehicles(),
                fleetSummary.getAvgUtilization(),
                fleetSummary.getTotalDistance(),
                fleetSummary.getLastUpdated()
        );
    }

    @Override
    public TCOAnalysisResponse getTCOAnalysis(String vehicleId) {
        TCOAnalysis tcoAnalysis = tcoAnalysisRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new RuntimeException("TCO analysis not found for vehicle: " + vehicleId));

        return new TCOAnalysisResponse(
                tcoAnalysis.getVehicleId(),
                tcoAnalysis.getTotalEnergyCost(),
                tcoAnalysis.getTotalMaintenanceCost(),
                tcoAnalysis.getTotalOwnershipCost(),
                tcoAnalysis.getAvgCostPerKm(),
                tcoAnalysis.getAnalysisPeriods()
        );
    }

    @Override
    public List<CostAnalytics> getCostAnalytics(String companyId) {
        return costAnalyticsRepository.findByCompanyId(companyId);
    }

    @Override
    public List<UtilizationReport> getUtilizationReports(String vehicleId) {
        return utilizationReportRepository.findByVehicleId(vehicleId);
    }
}
