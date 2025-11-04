package com.evfleet.maintenance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceSchedule {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "service_type", nullable = false, length = 100)
    private String serviceType;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "due_mileage")
    private Integer dueMileage;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "priority", length = 50)
    private String priority;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "SCHEDULED";
        }
        if (priority == null) {
            priority = "MEDIUM";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
