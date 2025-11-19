package com.evfleet.fleet.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Battery Health Entity
 *
 * Tracks battery health metrics over time for EV and Hybrid vehicles.
 * Includes State of Health (SOH), cycle count, temperature, and other diagnostics.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "battery_health", indexes = {
    @Index(name = "idx_vehicle_id", columnList = "vehicle_id"),
    @Index(name = "idx_recorded_at", columnList = "recorded_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatteryHealth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    /**
     * State of Health (SOH) - Percentage of original capacity remaining (0-100%)
     * e.g., 95% means battery retains 95% of original capacity
     */
    @Column(name = "soh", nullable = false)
    private Double soh;

    /**
     * Number of charge/discharge cycles
     */
    @Column(name = "cycle_count", nullable = false)
    private Integer cycleCount;

    /**
     * Battery temperature in Celsius
     */
    @Column(name = "temperature")
    private Double temperature;

    /**
     * Internal resistance in milli-ohms (mΩ)
     * Higher values indicate degradation
     */
    @Column(name = "internal_resistance")
    private Double internalResistance;

    /**
     * Voltage deviation from nominal (in volts)
     * Used to detect bad cells
     */
    @Column(name = "voltage_deviation")
    private Double voltageDeviation;

    /**
     * Current State of Charge (0-100%)
     */
    @Column(name = "current_soc")
    private Double currentSoc;

    /**
     * Timestamp when this health data was recorded
     */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    /**
     * Optional notes from the system or technician
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
