package com.evfleet.analytics.repository;

import com.evfleet.analytics.entity.FleetSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FleetSummaryRepository extends JpaRepository<FleetSummary, String> {

    Optional<FleetSummary> findByCompanyId(String companyId);
}
