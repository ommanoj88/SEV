package com.evfleet.maintenance.repository;

import com.evfleet.maintenance.model.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    List<MaintenanceRecord> findByVehicleId(Long vehicleId);
    List<MaintenanceRecord> findByCompanyId(Long companyId);
    List<MaintenanceRecord> findByStatus(MaintenanceRecord.MaintenanceStatus status);
    List<MaintenanceRecord> findByCompanyIdAndStatusAndScheduledDateAfter(
            Long companyId, MaintenanceRecord.MaintenanceStatus status, java.time.LocalDate date);
    
    Optional<MaintenanceRecord> findTopByVehicleIdAndTypeAndStatusOrderByCompletedDateDesc(
            Long vehicleId, MaintenanceRecord.MaintenanceType type, MaintenanceRecord.MaintenanceStatus status);
    
    boolean existsByVehicleIdAndTypeAndPolicyIdAndStatusIn(
            Long vehicleId, MaintenanceRecord.MaintenanceType type, Long policyId, List<MaintenanceRecord.MaintenanceStatus> statuses);
}
