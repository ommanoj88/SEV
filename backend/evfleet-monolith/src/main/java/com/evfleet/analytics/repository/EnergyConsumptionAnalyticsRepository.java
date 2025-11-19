package com.evfleet.analytics.repository;

import com.evfleet.analytics.model.EnergyConsumptionAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Energy Consumption Analytics operations
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface EnergyConsumptionAnalyticsRepository extends JpaRepository<EnergyConsumptionAnalytics, Long> {

    /**
     * Find energy analytics by vehicle ID and date
     */
    Optional<EnergyConsumptionAnalytics> findByVehicleIdAndAnalysisDate(Long vehicleId, LocalDate analysisDate);

    /**
     * Find all energy analytics for a vehicle
     */
    List<EnergyConsumptionAnalytics> findByVehicleIdOrderByAnalysisDateDesc(Long vehicleId);

    /**
     * Find energy analytics by vehicle ID within date range
     */
    List<EnergyConsumptionAnalytics> findByVehicleIdAndAnalysisDateBetween(
            Long vehicleId, LocalDate startDate, LocalDate endDate);

    /**
     * Find all energy analytics for a company
     */
    List<EnergyConsumptionAnalytics> findByCompanyId(Long companyId);

    /**
     * Find energy analytics by company ID within date range
     */
    List<EnergyConsumptionAnalytics> findByCompanyIdAndAnalysisDateBetween(
            Long companyId, LocalDate startDate, LocalDate endDate);

    /**
     * Find the latest energy analytics for a vehicle
     */
    @Query("SELECT e FROM EnergyConsumptionAnalytics e WHERE e.vehicleId = :vehicleId ORDER BY e.analysisDate DESC LIMIT 1")
    Optional<EnergyConsumptionAnalytics> findLatestByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Calculate total energy consumed by a vehicle within date range
     */
    @Query("SELECT SUM(e.totalEnergyConsumed) FROM EnergyConsumptionAnalytics e " +
           "WHERE e.vehicleId = :vehicleId AND e.analysisDate BETWEEN :startDate AND :endDate")
    Optional<java.math.BigDecimal> calculateTotalEnergyConsumed(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Calculate average efficiency for a vehicle within date range
     */
    @Query("SELECT AVG(e.averageEfficiency) FROM EnergyConsumptionAnalytics e " +
           "WHERE e.vehicleId = :vehicleId AND e.analysisDate BETWEEN :startDate AND :endDate")
    Optional<java.math.BigDecimal> calculateAverageEfficiency(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Delete all energy analytics older than a specific date
     */
    void deleteByAnalysisDateBefore(LocalDate date);
}
