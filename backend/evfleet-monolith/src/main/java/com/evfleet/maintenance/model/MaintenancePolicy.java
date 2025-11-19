package com.evfleet.maintenance.model;

import com.evfleet.common.entity.BaseEntity;
import com.evfleet.fleet.model.Vehicle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Maintenance Policy Entity
 * Defines rules for automatic maintenance scheduling based on mileage or time intervals
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "maintenance_policies", indexes = {
    @Index(name = "idx_policy_vehicle_type", columnList = "vehicle_type"),
    @Index(name = "idx_policy_company", columnList = "company_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenancePolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "company_id")
    private Long companyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "maintenance_type", nullable = false)
    private MaintenanceRecord.MaintenanceType maintenanceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    private Vehicle.VehicleType vehicleType;

    /**
     * Mileage interval in kilometers (e.g., 5000 for every 5000 km)
     */
    @Column(name = "mileage_interval_km")
    private Double mileageIntervalKm;

    /**
     * Time interval in days (e.g., 30 for monthly, 365 for yearly)
     */
    @Column(name = "time_interval_days")
    private Integer timeIntervalDays;

    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Check if this policy should trigger based on vehicle's total distance
     */
    public boolean shouldTriggerByMileage(Double lastMaintenanceDistance, Double currentDistance) {
        if (mileageIntervalKm == null || mileageIntervalKm <= 0) {
            return false;
        }
        
        double distanceSinceLastMaintenance = currentDistance - (lastMaintenanceDistance != null ? lastMaintenanceDistance : 0.0);
        return distanceSinceLastMaintenance >= mileageIntervalKm;
    }
}
