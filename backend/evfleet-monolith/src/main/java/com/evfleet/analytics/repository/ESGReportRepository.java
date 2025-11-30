package com.evfleet.analytics.repository;

import com.evfleet.analytics.model.ESGReport;
import com.evfleet.analytics.model.ESGReport.ComplianceStandard;
import com.evfleet.analytics.model.ESGReport.ReportStatus;
import com.evfleet.analytics.model.ESGReport.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ESG Report operations
 * 
 * Provides data access methods for Environmental, Social, and Governance reports.
 * Supports querying by company, period, report type, and compliance standard.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface ESGReportRepository extends JpaRepository<ESGReport, Long> {

    // ========== FIND BY COMPANY ==========

    /**
     * Find all ESG reports for a company
     */
    List<ESGReport> findByCompanyId(Long companyId);

    /**
     * Find ESG reports for a company with pagination
     */
    Page<ESGReport> findByCompanyId(Long companyId, Pageable pageable);

    /**
     * Find ESG reports by company ordered by period end date
     */
    List<ESGReport> findByCompanyIdOrderByPeriodEndDesc(Long companyId);

    // ========== FIND BY PERIOD ==========

    /**
     * Find ESG reports for a company within a date range
     */
    List<ESGReport> findByCompanyIdAndPeriodStartGreaterThanEqualAndPeriodEndLessThanEqual(
            Long companyId, LocalDate startDate, LocalDate endDate);

    /**
     * Find ESG reports overlapping with a date range
     */
    @Query("SELECT e FROM ESGReport e WHERE e.companyId = :companyId " +
           "AND e.periodStart <= :endDate AND e.periodEnd >= :startDate " +
           "ORDER BY e.periodStart DESC")
    List<ESGReport> findOverlappingReports(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ========== FIND BY TYPE ==========

    /**
     * Find ESG reports by report type
     */
    List<ESGReport> findByCompanyIdAndReportType(Long companyId, ReportType reportType);

    /**
     * Find ESG reports by compliance standard
     */
    List<ESGReport> findByCompanyIdAndComplianceStandard(Long companyId, ComplianceStandard complianceStandard);

    /**
     * Find ESG reports by status
     */
    List<ESGReport> findByCompanyIdAndStatus(Long companyId, ReportStatus status);

    // ========== LATEST REPORTS ==========

    /**
     * Find the latest ESG report for a company
     */
    Optional<ESGReport> findFirstByCompanyIdOrderByPeriodEndDesc(Long companyId);

    /**
     * Find the latest report of a specific type
     */
    Optional<ESGReport> findFirstByCompanyIdAndReportTypeOrderByPeriodEndDesc(
            Long companyId, ReportType reportType);

    /**
     * Find the latest approved report
     */
    Optional<ESGReport> findFirstByCompanyIdAndStatusOrderByPeriodEndDesc(
            Long companyId, ReportStatus status);

    // ========== AGGREGATION QUERIES ==========

    /**
     * Get total emissions for a company over a period
     */
    @Query("SELECT SUM(e.totalEmissionsKg) FROM ESGReport e " +
           "WHERE e.companyId = :companyId " +
           "AND e.periodStart >= :startDate AND e.periodEnd <= :endDate")
    BigDecimal getTotalEmissionsForPeriod(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get total carbon savings for a company over a period
     */
    @Query("SELECT SUM(e.carbonSavingsKg) FROM ESGReport e " +
           "WHERE e.companyId = :companyId " +
           "AND e.periodStart >= :startDate AND e.periodEnd <= :endDate")
    BigDecimal getTotalCarbonSavingsForPeriod(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get average emissions reduction percentage
     */
    @Query("SELECT AVG(e.emissionsReductionPercent) FROM ESGReport e " +
           "WHERE e.companyId = :companyId " +
           "AND e.periodStart >= :startDate AND e.periodEnd <= :endDate")
    BigDecimal getAverageEmissionsReductionPercent(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get emissions trend over time (monthly averages)
     */
    @Query("SELECT MONTH(e.periodEnd) as month, YEAR(e.periodEnd) as year, " +
           "AVG(e.totalEmissionsKg) as avgEmissions, " +
           "AVG(e.carbonSavingsKg) as avgSavings " +
           "FROM ESGReport e " +
           "WHERE e.companyId = :companyId " +
           "AND e.periodStart >= :startDate AND e.periodEnd <= :endDate " +
           "GROUP BY YEAR(e.periodEnd), MONTH(e.periodEnd) " +
           "ORDER BY YEAR(e.periodEnd), MONTH(e.periodEnd)")
    List<Object[]> getEmissionsTrendMonthly(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ========== CHECK EXISTENCE ==========

    /**
     * Check if a report exists for a specific period
     */
    boolean existsByCompanyIdAndReportTypeAndPeriodStartAndPeriodEnd(
            Long companyId, ReportType reportType, LocalDate periodStart, LocalDate periodEnd);

    /**
     * Count reports by company and status
     */
    long countByCompanyIdAndStatus(Long companyId, ReportStatus status);

    // ========== DELETE ==========

    /**
     * Delete draft reports older than a specific date
     */
    @Query("DELETE FROM ESGReport e WHERE e.companyId = :companyId " +
           "AND e.status = 'DRAFT' AND e.createdAt < :cutoffDate")
    void deleteOldDraftReports(@Param("companyId") Long companyId, @Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
