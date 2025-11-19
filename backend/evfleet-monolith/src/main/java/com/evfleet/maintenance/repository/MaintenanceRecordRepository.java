package com.evfleet.maintenance.repository;

import com.evfleet.maintenance.model.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    List<MaintenanceRecord> findByVehicleId(Long vehicleId);
    List<MaintenanceRecord> findByCompanyId(Long companyId);
    List<MaintenanceRecord> findByStatus(MaintenanceRecord.MaintenanceStatus status);
    List<MaintenanceRecord> findByCompanyIdAndStatusAndScheduledDateAfter(
            Long companyId, MaintenanceRecord.MaintenanceStatus status, LocalDate date);
    
    /**
     * Find maintenance alerts for a company within the next N days
     * Excludes COMPLETED and CANCELLED records
     */
    @Query("SELECT m FROM MaintenanceRecord m WHERE m.companyId = :companyId " +
           "AND m.scheduledDate <= :endDate " +
           "AND m.status NOT IN ('COMPLETED', 'CANCELLED') " +
           "ORDER BY m.scheduledDate ASC")
    List<MaintenanceRecord> findUpcomingMaintenanceAlerts(Long companyId, LocalDate endDate);
    
    Optional<MaintenanceRecord> findTopByVehicleIdAndTypeAndStatusOrderByCompletedDateDesc(
            Long vehicleId, MaintenanceRecord.MaintenanceType type, MaintenanceRecord.MaintenanceStatus status);
    
    boolean existsByVehicleIdAndTypeAndPolicyIdAndStatusIn(
            Long vehicleId, MaintenanceRecord.MaintenanceType type, Long policyId, List<MaintenanceRecord.MaintenanceStatus> statuses);
}
