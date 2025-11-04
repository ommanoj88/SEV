package com.evfleet.maintenance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "service_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHistory {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "service_type", nullable = false, length = 100)
    private String serviceType;

    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "service_center")
    private String serviceCenter;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "parts_replaced", columnDefinition = "TEXT[]")
    private List<String> partsReplaced;

    @Column(name = "technician")
    private String technician;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
