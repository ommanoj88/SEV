package com.evfleet.customer.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer Entity
 *
 * Represents a customer in the fleet management system.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customer_company", columnList = "company_id"),
    @Index(name = "idx_customer_type", columnList = "customer_type"),
    @Index(name = "idx_customer_email", columnList = "email"),
    @Index(name = "idx_customer_phone", columnList = "phone")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 20)
    private CustomerType customerType;

    // Basic Information
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 100)
    private String country;

    // Business Customer Specific
    @Column(name = "gstin", length = 15)
    private String gstin; // Goods and Services Tax Identification Number

    @Column(name = "pan", length = 10)
    private String pan; // Permanent Account Number

    @Column(name = "business_name", length = 300)
    private String businessName;

    // Individual Customer Specific
    @Column(name = "date_of_birth")
    private String dateOfBirth;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * Customer types
     */
    public enum CustomerType {
        INDIVIDUAL,  // Individual customer
        BUSINESS     // Business/Corporate customer
    }

    /**
     * Customer status
     */
    public enum CustomerStatus {
        ACTIVE,      // Active customer
        INACTIVE,    // Inactive customer
        BLOCKED      // Blocked customer
    }
}
