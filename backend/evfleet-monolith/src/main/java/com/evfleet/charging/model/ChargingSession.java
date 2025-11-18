package com.evfleet.charging.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Charging Session Entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "charging_sessions", indexes = {
    @Index(name = "idx_session_vehicle", columnList = "vehicle_id"),
    @Index(name = "idx_session_station", columnList = "station_id"),
    @Index(name = "idx_session_status", columnList = "status"),
    @Index(name = "idx_session_start_time", columnList = "start_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "energy_consumed")
    private BigDecimal energyConsumed; // in kWh

    @Column(name = "cost")
    private BigDecimal cost; // in INR

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "initial_soc")
    private Double initialSoc; // State of Charge at start

    @Column(name = "final_soc")
    private Double finalSoc; // State of Charge at end

    @Column(length = 500)
    private String notes;

    public enum SessionStatus {
        ACTIVE,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    public void complete(BigDecimal energyKwh, BigDecimal totalCost, Double finalSoc) {
        if (status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Can only complete active sessions");
        }
        this.endTime = LocalDateTime.now();
        this.energyConsumed = energyKwh;
        this.cost = totalCost;
        this.finalSoc = finalSoc;
        this.status = SessionStatus.COMPLETED;
    }

    public void fail(String reason) {
        this.endTime = LocalDateTime.now();
        this.status = SessionStatus.FAILED;
        this.notes = reason;
    }
}
