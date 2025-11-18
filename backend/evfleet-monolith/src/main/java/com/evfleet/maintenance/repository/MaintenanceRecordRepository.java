package com.evfleet.maintenance.repository;

import com.evfleet.maintenance.model.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    List<MaintenanceRecord> findByVehicleId(Long vehicleId);
    List<MaintenanceRecord> findByCompanyId(Long companyId);
    List<MaintenanceRecord> findByStatus(MaintenanceRecord.MaintenanceStatus status);
    List<MaintenanceRecord> findByCompanyIdAndStatusAndScheduledDateAfter(
            Long companyId, MaintenanceRecord.MaintenanceStatus status, java.time.LocalDate date);
}
