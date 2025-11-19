package com.evfleet.analytics.scheduler;

import com.evfleet.analytics.service.EnergyAnalyticsService;
import com.evfleet.analytics.service.TCOAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Analytics Aggregation Scheduler
 * Runs scheduled jobs for analytics data aggregation
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsAggregationScheduler {

    private final EnergyAnalyticsService energyAnalyticsService;
    private final TCOAnalysisService tcoAnalysisService;

    /**
     * Daily energy analytics aggregation
     * Runs at 1 AM every day
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void aggregateDailyEnergyAnalytics() {
        log.info("Starting daily energy analytics aggregation job");
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            energyAnalyticsService.aggregateEnergyAnalyticsForAllVehicles(yesterday);
            log.info("Daily energy analytics aggregation job completed successfully");
        } catch (Exception e) {
            log.error("Error in daily energy analytics aggregation job", e);
        }
    }

    /**
     * Weekly TCO recalculation
     * Runs at 2 AM every Sunday
     */
    @Scheduled(cron = "0 0 2 ? * SUN")
    public void recalculateTCO() {
        log.info("Starting weekly TCO recalculation job");
        try {
            tcoAnalysisService.recalculateTCOForAllVehicles();
            log.info("Weekly TCO recalculation job completed successfully");
        } catch (Exception e) {
            log.error("Error in weekly TCO recalculation job", e);
        }
    }

    /**
     * Monthly analytics cleanup
     * Runs at 3 AM on the 1st of every month
     * Cleans up old analytics data based on retention policy
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void cleanupOldAnalytics() {
        log.info("Starting monthly analytics cleanup job");
        try {
            // TODO: Implement cleanup logic based on retention policy
            // For example, delete analytics older than 2 years
            log.info("Monthly analytics cleanup job completed successfully");
        } catch (Exception e) {
            log.error("Error in monthly analytics cleanup job", e);
        }
    }
}
