package com.evfleet.billing.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_company_id", columnList = "company_id"),
    @Index(name = "idx_invoice_number", columnList = "invoice_number"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_due_date", columnList = "due_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    public enum InvoiceStatus {
        DRAFT,
        PENDING,
        PAID,
        PARTIALLY_PAID,
        OVERDUE,
        CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = InvoiceStatus.PENDING;
        }
        calculateTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotal();
        checkOverdue();
    }

    private void calculateTotal() {
        if (subtotal != null) {
            totalAmount = subtotal
                .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        }
    }

    private void checkOverdue() {
        if (status == InvoiceStatus.PENDING && dueDate != null && dueDate.isBefore(LocalDate.now())) {
            status = InvoiceStatus.OVERDUE;
        }
    }

    public void markAsPaid(LocalDate paidDate, BigDecimal amount) {
        this.paidDate = paidDate;
        this.paidAmount = amount;
        if (amount.compareTo(totalAmount) >= 0) {
            this.status = InvoiceStatus.PAID;
        } else {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        }
    }

    public void cancel(String reason) {
        this.status = InvoiceStatus.CANCELLED;
        this.remarks = reason;
    }

    public boolean isOverdue() {
        return status == InvoiceStatus.OVERDUE ||
               (status == InvoiceStatus.PENDING && dueDate != null && dueDate.isBefore(LocalDate.now()));
    }

    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount != null ? paidAmount : BigDecimal.ZERO);
    }
}
