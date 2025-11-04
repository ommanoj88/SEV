package com.evfleet.driver.entity;

import com.evfleet.driver.enums.DriverStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "license_number", unique = true, nullable = false, length = 100)
    private String licenseNumber;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private DriverStatus status = DriverStatus.ACTIVE;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.valueOf(4.0);

    @Column(name = "total_trips")
    private Integer totalTrips = 0;

    @Column(name = "joined_date")
    private LocalDate joinedDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (joinedDate == null) {
            joinedDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
