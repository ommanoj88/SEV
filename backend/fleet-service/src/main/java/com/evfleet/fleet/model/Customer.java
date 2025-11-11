package com.evfleet.fleet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Customer code is required")
    @Column(name = "customer_code", unique = true, nullable = false, length = 50)
    private String customerCode;

    @NotBlank(message = "Customer name is required")
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_type", length = 50)
    private String customerType; // 'INDIVIDUAL', 'BUSINESS'

    // Contact Information
    @Column(name = "primary_contact_name")
    private String primaryContactName;

    @Column(name = "primary_phone", length = 20)
    private String primaryPhone;

    @Column(name = "secondary_phone", length = 20)
    private String secondaryPhone;

    @Email(message = "Invalid email format")
    @Column(length = 100)
    private String email;

    // Address
    @Column(name = "address_line1", columnDefinition = "TEXT")
    private String addressLine1;

    @Column(name = "address_line2", columnDefinition = "TEXT")
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country = "India";

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    // Business Details
    @Column(length = 15)
    private String gstin;

    @Column(length = 10)
    private String pan;

    // Preferences
    @Column(name = "preferred_delivery_time", length = 50)
    private String preferredDeliveryTime;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    // Status
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "outstanding_balance", precision = 12, scale = 2)
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    // Ratings and Statistics
    @Column(name = "service_rating", precision = 3, scale = 2)
    private BigDecimal serviceRating; // Average rating

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    @Column(name = "successful_deliveries")
    private Integer successfulDeliveries = 0;

    @Column(name = "failed_deliveries")
    private Integer failedDeliveries = 0;

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Helper methods
    public boolean isIndividual() {
        return "INDIVIDUAL".equalsIgnoreCase(customerType);
    }

    public boolean isBusiness() {
        return "BUSINESS".equalsIgnoreCase(customerType);
    }

    public BigDecimal getSuccessRate() {
        if (totalDeliveries == null || totalDeliveries == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(successfulDeliveries)
                .divide(BigDecimal.valueOf(totalDeliveries), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    public boolean hasCreditLimit() {
        return creditLimit != null && creditLimit.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isOverCreditLimit() {
        if (creditLimit == null || outstandingBalance == null) {
            return false;
        }
        return outstandingBalance.compareTo(creditLimit) > 0;
    }

    public BigDecimal getAvailableCredit() {
        if (creditLimit == null || outstandingBalance == null) {
            return BigDecimal.ZERO;
        }
        return creditLimit.subtract(outstandingBalance);
    }
}
