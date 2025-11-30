package com.evfleet.telematics.scheduler;

import com.evfleet.telematics.repository.TelemetrySnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled job to enforce telemetry data retention policy.
 * Deletes telemetry snapshots older than configured retention period.
 * 
 * Default retention: 90 days
 * Runs daily at 2 AM to minimize impact on system performance.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TelemetryRetentionScheduler {

    private final TelemetrySnapshotRepository telemetrySnapshotRepository;

    @Value("${evfleet.telematics.retention.days:90}")
    private int retentionDays;

    @Value("${evfleet.telematics.retention.batch-size:10000}")
    private int batchSize;

    @Value("${evfleet.telematics.retention.enabled:true}")
    private boolean retentionEnabled;

    /**
     * Run retention cleanup daily at 2 AM.
     * Uses batch deletion to avoid long-running transactions.
     */
    @Scheduled(cron = "${evfleet.telematics.retention.cron:0 0 2 * * ?}")
    @Transactional
    public void cleanupOldTelemetryData() {
        if (!retentionEnabled) {
            log.info("Telemetry retention cleanup is disabled");
            return;
        }

        log.info("Starting telemetry retention cleanup - retention period: {} days", retentionDays);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        
        try {
            // First, count how many records will be deleted (for logging)
            long recordsToDelete = telemetrySnapshotRepository.countByTimestampBefore(cutoffDate);
            
            if (recordsToDelete == 0) {
                log.info("No telemetry records older than {} days found", retentionDays);
                return;
            }

            log.info("Found {} telemetry records older than {} to delete", recordsToDelete, cutoffDate);

            // Delete in batches to avoid overwhelming the database
            int totalDeleted = 0;
            int batchDeleted;
            int batchNumber = 0;
            
            do {
                batchNumber++;
                batchDeleted = telemetrySnapshotRepository.deleteByTimestampBefore(cutoffDate);
                totalDeleted += batchDeleted;
                
                if (batchDeleted > 0) {
                    log.debug("Batch {}: Deleted {} telemetry records", batchNumber, batchDeleted);
                }
                
                // Small delay between batches to reduce database load
                if (batchDeleted >= batchSize) {
                    Thread.sleep(100);
                }
                
            } while (batchDeleted >= batchSize && batchNumber < 1000); // Safety limit

            log.info("✅ Telemetry retention cleanup complete - deleted {} records in {} batches", 
                totalDeleted, batchNumber);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Telemetry retention cleanup interrupted", e);
        } catch (Exception e) {
            log.error("Error during telemetry retention cleanup", e);
        }
    }

    /**
     * Manual trigger for retention cleanup (for admin use)
     * @return Number of records deleted
     */
    @Transactional
    public int runManualCleanup() {
        log.info("Manual telemetry retention cleanup triggered");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        int deleted = telemetrySnapshotRepository.deleteByTimestampBefore(cutoffDate);
        
        log.info("Manual cleanup deleted {} records older than {}", deleted, cutoffDate);
        return deleted;
    }

    /**
     * Get retention statistics
     */
    @Transactional(readOnly = true)
    public RetentionStats getRetentionStats() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        
        long totalRecords = telemetrySnapshotRepository.count();
        long expiredRecords = telemetrySnapshotRepository.countByTimestampBefore(cutoffDate);
        
        return new RetentionStats(
            retentionDays,
            totalRecords,
            expiredRecords,
            cutoffDate,
            retentionEnabled
        );
    }

    /**
     * DTO for retention statistics
     */
    public record RetentionStats(
        int retentionDays,
        long totalRecords,
        long expiredRecords,
        LocalDateTime cutoffDate,
        boolean retentionEnabled
    ) {}
}
