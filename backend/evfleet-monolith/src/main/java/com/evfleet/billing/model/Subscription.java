package com.evfleet.billing.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Subscription Entity
 * Represents a company's subscription plan
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_company_id", columnList = "company_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "plan_type", nullable = false, length = 100)
    private String planType;  // BASIC, PRO, ENTERPRISE

    @Column(name = "vehicle_count", nullable = false)
    private Integer vehicleCount;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "billing_cycle", nullable = false, length = 50)
    private String billingCycle;  // MONTHLY, QUARTERLY, YEARLY

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "auto_renew")
    @Builder.Default
    private Boolean autoRenew = true;

    public enum SubscriptionStatus {
        ACTIVE,
        INACTIVE,
        CANCELLED,
        EXPIRED
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = SubscriptionStatus.ACTIVE;
        }
        if (autoRenew == null) {
            autoRenew = true;
        }
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        this.endDate = LocalDate.now();
    }

    public void renew(LocalDate newEndDate) {
        this.startDate = this.endDate != null ? this.endDate : LocalDate.now();
        this.endDate = newEndDate;
        this.status = SubscriptionStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE &&
               (endDate == null || endDate.isAfter(LocalDate.now()));
    }
}
