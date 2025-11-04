package com.evfleet.analytics.repository;

import com.evfleet.analytics.entity.UtilizationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UtilizationReportRepository extends JpaRepository<UtilizationReport, String> {

    List<UtilizationReport> findByVehicleId(String vehicleId);
}
