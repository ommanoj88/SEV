package com.evfleet.billing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expense_number", unique = true, nullable = false, length = 50)
    private String expenseNumber;

    // Associated Entity
    @NotBlank(message = "Entity type is required")
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // 'VEHICLE', 'DRIVER', 'TRIP', 'FLEET'

    @NotNull(message = "Entity ID is required")
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "trip_id")
    private Long tripId;

    // Expense Details
    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @Column(length = 100)
    private String subcategory;

    @NotBlank(message = "Description is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "INR";

    // Date Information
    @NotNull(message = "Expense date is required")
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    // Vendor/Payee
    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "vendor_contact", length = 100)
    private String vendorContact;

    // Receipt Information
    @Column(name = "receipt_number", length = 100)
    private String receiptNumber;

    @Column(name = "receipt_file_path", length = 500)
    private String receiptFilePath;

    @Column(name = "receipt_file_name")
    private String receiptFileName;

    // Payment Information
    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // 'CASH', 'CARD', 'UPI', 'BANK_TRANSFER', etc.

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    // Approval Workflow
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // Reimbursement
    @Column(name = "is_reimbursable")
    private Boolean isReimbursable = false;

    @Column(name = "reimbursed")
    private Boolean reimbursed = false;

    @Column(name = "reimbursement_date")
    private LocalDate reimbursementDate;

    // Additional Info
    @Column(name = "odometer_reading")
    private Integer odometerReading;

    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT[]")
    private String[] tags;

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
    public boolean isPending() {
        return status == ExpenseStatus.PENDING_APPROVAL;
    }

    public boolean isApproved() {
        return status == ExpenseStatus.APPROVED;
    }

    public boolean canBeApproved() {
        return status == ExpenseStatus.PENDING_APPROVAL;
    }

    public boolean canBeRejected() {
        return status == ExpenseStatus.PENDING_APPROVAL;
    }
}
