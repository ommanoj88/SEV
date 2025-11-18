package com.evfleet.charging.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Charging Station Entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "charging_stations", indexes = {
    @Index(name = "idx_station_status", columnList = "status"),
    @Index(name = "idx_station_location", columnList = "latitude, longitude")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingStation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "total_slots", nullable = false)
    private Integer totalSlots;

    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StationStatus status;

    @Column(name = "charger_type", length = 50)
    private String chargerType; // CCS, CHAdeMO, Type2

    @Column(name = "power_output")
    private Double powerOutput; // in kW

    @Column(name = "price_per_kwh")
    private Double pricePerKwh;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    @Column(length = 20)
    private String phone;

    public enum StationStatus {
        AVAILABLE,
        FULL,
        MAINTENANCE,
        OFFLINE
    }

    public boolean hasAvailableSlots() {
        return availableSlots > 0;
    }

    public void reserveSlot() {
        if (availableSlots <= 0) {
            throw new IllegalStateException("No available slots");
        }
        availableSlots--;
        if (availableSlots == 0) {
            status = StationStatus.FULL;
        }
    }

    public void releaseSlot() {
        if (availableSlots < totalSlots) {
            availableSlots++;
            if (status == StationStatus.FULL) {
                status = StationStatus.AVAILABLE;
            }
        }
    }
}
