package com.evfleet.analytics.repository;

import com.evfleet.analytics.model.TCOAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TCO Analysis operations
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface TCOAnalysisRepository extends JpaRepository<TCOAnalysis, Long> {

    /**
     * Find TCO analysis by vehicle ID
     */
    Optional<TCOAnalysis> findByVehicleId(Long vehicleId);

    /**
     * Find TCO analysis by vehicle ID and analysis date
     */
    Optional<TCOAnalysis> findByVehicleIdAndAnalysisDate(Long vehicleId, LocalDate analysisDate);

    /**
     * Find all TCO analyses for a company
     */
    List<TCOAnalysis> findByCompanyId(Long companyId);

    /**
     * Find TCO analyses by vehicle ID within date range
     */
    List<TCOAnalysis> findByVehicleIdAndAnalysisDateBetween(
            Long vehicleId, LocalDate startDate, LocalDate endDate);

    /**
     * Find TCO analyses by company ID within date range
     */
    List<TCOAnalysis> findByCompanyIdAndAnalysisDateBetween(
            Long companyId, LocalDate startDate, LocalDate endDate);

    /**
     * Find the latest TCO analysis for a vehicle
     */
    @Query("SELECT t FROM TCOAnalysis t WHERE t.vehicleId = :vehicleId ORDER BY t.analysisDate DESC LIMIT 1")
    Optional<TCOAnalysis> findLatestByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Find all TCO analyses for a company ordered by date descending
     */
    List<TCOAnalysis> findByCompanyIdOrderByAnalysisDateDesc(Long companyId);

    /**
     * Delete all TCO analyses older than a specific date
     */
    void deleteByAnalysisDateBefore(LocalDate date);
}
