package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.FleetSummaryResponse;
import com.evfleet.analytics.dto.TCOAnalysisResponse;
import com.evfleet.analytics.entity.CostAnalytics;
import com.evfleet.analytics.entity.UtilizationReport;

import java.util.List;

public interface AnalyticsService {

    FleetSummaryResponse getFleetSummary(String companyId);

    TCOAnalysisResponse getTCOAnalysis(String vehicleId);

    List<CostAnalytics> getCostAnalytics(String companyId);

    List<UtilizationReport> getUtilizationReports(String vehicleId);
}
