package com.evfleet.maintenance.repository;

import com.evfleet.maintenance.model.MaintenanceLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for MaintenanceLineItem entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface MaintenanceLineItemRepository extends JpaRepository<MaintenanceLineItem, Long> {

    List<MaintenanceLineItem> findByMaintenanceRecordId(Long maintenanceRecordId);

    List<MaintenanceLineItem> findByMaintenanceRecordIdAndType(Long maintenanceRecordId, MaintenanceLineItem.LineItemType type);

    @Query("SELECT SUM(li.totalPrice) FROM MaintenanceLineItem li WHERE li.maintenanceRecordId = :recordId")
    BigDecimal calculateTotalCostForRecord(Long recordId);
}
