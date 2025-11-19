package com.evfleet.maintenance.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType type;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Column
    private LocalDate completedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus status;

    @Column
    private BigDecimal cost;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String serviceProvider;

    /**
     * Vehicle's total distance (odometer reading) at the time of maintenance
     */
    @Column(name = "vehicle_distance_km")
    private Double vehicleDistanceKm;

    /**
     * Reference to the maintenance policy that triggered this record (if auto-generated)
     */
    @Column(name = "policy_id")
    private Long policyId;

    /**
     * URLs or paths to attached documents (invoices, photos, reports)
     * Stored as comma-separated list
     */
    @Column(name = "attachment_urls", length = 2000)
    private String attachmentUrls;

    public enum MaintenanceType {
        // Common types (applicable to all vehicle types)
        ROUTINE_SERVICE,
        TIRE_REPLACEMENT,
        BRAKE_SERVICE,
        EMERGENCY_REPAIR,
        
        // ICE-specific types
        OIL_CHANGE,
        FILTER_REPLACEMENT,
        EMISSION_TEST,
        COOLANT_FLUSH,
        TRANSMISSION_SERVICE,
        ENGINE_DIAGNOSTICS,
        
        // EV-specific types
        BATTERY_CHECK,
        HV_SYSTEM_CHECK,
        FIRMWARE_UPDATE,
        CHARGING_PORT_INSPECTION,
        THERMAL_MANAGEMENT_CHECK,
        
        // Hybrid-specific (can use both ICE and EV types)
        HYBRID_SYSTEM_CHECK
    }

    public enum MaintenanceStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
