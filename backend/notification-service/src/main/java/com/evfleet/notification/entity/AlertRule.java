package com.evfleet.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alert_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertRule {

    @Id
    @Column(name = "id", length = 255)
    private String id;

    @Column(name = "company_id", nullable = false, length = 255)
    private String companyId;

    @Column(name = "rule_name", nullable = false, length = 255)
    private String ruleName;

    @Column(name = "trigger_type", nullable = false, length = 100)
    private String triggerType;

    @Column(name = "conditions", nullable = false, columnDefinition = "TEXT")
    private String conditions;

    @Column(name = "actions", nullable = false, columnDefinition = "TEXT")
    private String actions;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "schedule", length = 255)
    private String schedule;

    @Column(name = "created_by", length = 255)
    private String createdBy;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
