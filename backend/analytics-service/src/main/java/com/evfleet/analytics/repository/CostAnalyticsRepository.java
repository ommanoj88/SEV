package com.evfleet.analytics.repository;

import com.evfleet.analytics.entity.CostAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CostAnalyticsRepository extends JpaRepository<CostAnalytics, String> {

    List<CostAnalytics> findByCompanyId(String companyId);

    List<CostAnalytics> findByVehicleId(String vehicleId);
}
