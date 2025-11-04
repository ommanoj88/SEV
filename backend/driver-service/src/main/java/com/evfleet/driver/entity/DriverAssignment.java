package com.evfleet.driver.entity;

import com.evfleet.driver.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignment {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "shift_start", nullable = false)
    private LocalDateTime shiftStart;

    @Column(name = "shift_end")
    private LocalDateTime shiftEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private AssignmentStatus status = AssignmentStatus.ACTIVE;

    @Column(name = "assigned_by")
    private String assignedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
