package com.evfleet.charging.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "charging_stations", indexes = {
    @Index(name = "idx_station_status", columnList = "status"),
    @Index(name = "idx_station_provider", columnList = "provider"),
    @Index(name = "idx_station_location", columnList = "latitude,longitude")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Station name is required")
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank(message = "Address is required")
    @Column(nullable = false, length = 500)
    private String address;

    @NotNull(message = "Latitude is required")
    @Column(nullable = false)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Column(nullable = false)
    private Double longitude;

    @NotBlank(message = "Provider is required")
    @Column(nullable = false, length = 100)
    private String provider; // e.g., ChargePoint, EVgo, Tesla

    @Min(value = 0, message = "Available slots cannot be negative")
    @Column(nullable = false)
    private Integer availableSlots = 0;

    @Min(value = 1, message = "Total slots must be at least 1")
    @Column(nullable = false)
    private Integer totalSlots;

    @DecimalMin(value = "0.0", message = "Charging rate must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal chargingRate; // kW

    @DecimalMin(value = "0.0", message = "Price per kWh must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKwh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StationStatus status = StationStatus.ACTIVE;

    @Column(length = 50)
    private String connectorType; // Type 1, Type 2, CCS, CHAdeMO

    @Column(length = 1000)
    private String amenities; // WiFi, Restroom, Restaurant, etc.

    @Column(length = 20)
    private String operatingHours; // 24/7 or specific hours

    @Column(length = 100)
    private String contactPhone;

    @Column(length = 100)
    private String contactEmail;

    @Column(columnDefinition = "TEXT")
    private String apiEndpoint; // For integration with provider APIs

    @Column(columnDefinition = "TEXT")
    private String apiKey; // Encrypted in production

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum StationStatus {
        ACTIVE,
        INACTIVE,
        MAINTENANCE,
        FULL
    }

    public boolean hasAvailableSlots() {
        return availableSlots > 0 && status == StationStatus.ACTIVE;
    }

    public void reserveSlot() {
        if (hasAvailableSlots()) {
            availableSlots--;
            if (availableSlots == 0) {
                status = StationStatus.FULL;
            }
        }
    }

    public void releaseSlot() {
        if (availableSlots < totalSlots) {
            availableSlots++;
            if (status == StationStatus.FULL) {
                status = StationStatus.ACTIVE;
            }
        }
    }
}
