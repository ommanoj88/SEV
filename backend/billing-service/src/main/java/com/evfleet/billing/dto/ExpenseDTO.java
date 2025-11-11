package com.evfleet.billing.dto;

import com.evfleet.billing.entity.ExpenseCategory;
import com.evfleet.billing.entity.ExpenseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseDTO {

    private Long id;
    private String expenseNumber;

    @NotBlank(message = "Entity type is required")
    private String entityType;

    @NotNull(message = "Entity ID is required")
    private Long entityId;

    private Long vehicleId;
    private Long driverId;
    private Long tripId;

    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    private String subcategory;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String currency;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    private String vendorName;
    private String vendorContact;
    private String receiptNumber;
    private String receiptFilePath;
    private String receiptFileName;
    private String paymentMethod;
    private String paymentReference;
    private LocalDate paymentDate;

    private ExpenseStatus status;
    private String submittedBy;
    private LocalDateTime submittedAt;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;

    private Boolean isReimbursable;
    private Boolean reimbursed;
    private LocalDate reimbursementDate;

    private Integer odometerReading;
    private String location;
    private String notes;
    private String[] tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
