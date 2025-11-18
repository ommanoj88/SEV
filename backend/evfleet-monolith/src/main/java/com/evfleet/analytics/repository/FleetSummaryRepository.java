package com.evfleet.analytics.repository;

import com.evfleet.analytics.model.FleetSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FleetSummaryRepository extends JpaRepository<FleetSummary, Long> {
    List<FleetSummary> findByCompanyId(Long companyId);
    Optional<FleetSummary> findByCompanyIdAndSummaryDate(Long companyId, LocalDate summaryDate);
    List<FleetSummary> findByCompanyIdAndSummaryDateBetween(Long companyId, LocalDate startDate, LocalDate endDate);
}
