package com.evfleet.billing.service;

import com.evfleet.billing.dto.ExpenseDTO;
import com.evfleet.billing.entity.Expense;
import com.evfleet.billing.entity.ExpenseCategory;
import com.evfleet.billing.entity.ExpenseStatus;
import com.evfleet.billing.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseDTO createExpense(ExpenseDTO dto) {
        Expense expense = Expense.builder()
                .entityType(dto.getEntityType())
                .entityId(dto.getEntityId())
                .vehicleId(dto.getVehicleId())
                .driverId(dto.getDriverId())
                .tripId(dto.getTripId())
                .category(dto.getCategory())
                .subcategory(dto.getSubcategory())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "INR")
                .expenseDate(dto.getExpenseDate())
                .vendorName(dto.getVendorName())
                .vendorContact(dto.getVendorContact())
                .receiptNumber(dto.getReceiptNumber())
                .paymentMethod(dto.getPaymentMethod())
                .paymentReference(dto.getPaymentReference())
                .paymentDate(dto.getPaymentDate())
                .status(dto.getStatus() != null ? dto.getStatus() : ExpenseStatus.DRAFT)
                .submittedBy(dto.getSubmittedBy())
                .isReimbursable(dto.getIsReimbursable() != null ? dto.getIsReimbursable() : false)
                .reimbursed(false)
                .odometerReading(dto.getOdometerReading())
                .location(dto.getLocation())
                .notes(dto.getNotes())
                .tags(dto.getTags())
                .createdBy(dto.getCreatedBy())
                .build();

        Expense saved = expenseRepository.save(expense);
        log.info("Expense created: {} for {} ID: {}", saved.getCategory(), saved.getEntityType(), saved.getEntityId());

        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseDTO getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
        return convertToDTO(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getExpensesByEntity(String entityType, Long entityId) {
        return expenseRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getExpensesByVehicle(Long vehicleId) {
        return expenseRepository.findByVehicleId(vehicleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getExpensesByDriver(Long driverId) {
        return expenseRepository.findByDriverId(driverId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getExpensesByCategory(ExpenseCategory category) {
        return expenseRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getExpensesByStatus(ExpenseStatus status) {
        return expenseRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getPendingApprovals() {
        return expenseRepository.findPendingApprovals().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getUnreimbursedExpenses() {
        return expenseRepository.findUnreimbursedExpenses().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseDTO submitExpense(Long id, String submittedBy) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new RuntimeException("Only draft expenses can be submitted");
        }

        expense.setStatus(ExpenseStatus.PENDING_APPROVAL);
        expense.setSubmittedBy(submittedBy);
        expense.setSubmittedAt(LocalDateTime.now());

        Expense updated = expenseRepository.save(expense);
        log.info("Expense submitted for approval: {}", id);

        return convertToDTO(updated);
    }

    @Transactional
    public ExpenseDTO approveExpense(Long id, String approvedBy) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        if (!expense.canBeApproved()) {
            throw new RuntimeException("Expense cannot be approved in current status: " + expense.getStatus());
        }

        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setApprovedBy(approvedBy);
        expense.setApprovedAt(LocalDateTime.now());

        Expense updated = expenseRepository.save(expense);
        log.info("Expense approved: {} by {}", id, approvedBy);

        return convertToDTO(updated);
    }

    @Transactional
    public ExpenseDTO rejectExpense(Long id, String rejectedBy, String reason) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        if (!expense.canBeRejected()) {
            throw new RuntimeException("Expense cannot be rejected in current status: " + expense.getStatus());
        }

        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setApprovedBy(rejectedBy); // Using approvedBy field for rejection actor
        expense.setApprovedAt(LocalDateTime.now());
        expense.setRejectionReason(reason);

        Expense updated = expenseRepository.save(expense);
        log.info("Expense rejected: {} by {} - Reason: {}", id, rejectedBy, reason);

        return convertToDTO(updated);
    }

    @Transactional
    public ExpenseDTO updateExpense(Long id, ExpenseDTO dto) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        // Only update if expense is in DRAFT status
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new RuntimeException("Only draft expenses can be updated");
        }

        expense.setCategory(dto.getCategory());
        expense.setSubcategory(dto.getSubcategory());
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setVendorName(dto.getVendorName());
        expense.setVendorContact(dto.getVendorContact());
        expense.setReceiptNumber(dto.getReceiptNumber());
        expense.setPaymentMethod(dto.getPaymentMethod());
        expense.setPaymentReference(dto.getPaymentReference());
        expense.setIsReimbursable(dto.getIsReimbursable());
        expense.setOdometerReading(dto.getOdometerReading());
        expense.setLocation(dto.getLocation());
        expense.setNotes(dto.getNotes());
        expense.setTags(dto.getTags());
        expense.setUpdatedBy(dto.getUpdatedBy());

        Expense updated = expenseRepository.save(expense);
        log.info("Expense updated: {}", id);

        return convertToDTO(updated);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        // Only delete if expense is in DRAFT status
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new RuntimeException("Only draft expenses can be deleted");
        }

        expenseRepository.delete(expense);
        log.info("Expense deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalByCategory(ExpenseCategory category, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.calculateTotalByCategory(category, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalByEntity(String entityType, Long entityId) {
        return expenseRepository.calculateTotalByEntity(entityType, entityId);
    }

    private ExpenseDTO convertToDTO(Expense expense) {
        return ExpenseDTO.builder()
                .id(expense.getId())
                .expenseNumber(expense.getExpenseNumber())
                .entityType(expense.getEntityType())
                .entityId(expense.getEntityId())
                .vehicleId(expense.getVehicleId())
                .driverId(expense.getDriverId())
                .tripId(expense.getTripId())
                .category(expense.getCategory())
                .subcategory(expense.getSubcategory())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .expenseDate(expense.getExpenseDate())
                .vendorName(expense.getVendorName())
                .vendorContact(expense.getVendorContact())
                .receiptNumber(expense.getReceiptNumber())
                .receiptFilePath(expense.getReceiptFilePath())
                .receiptFileName(expense.getReceiptFileName())
                .paymentMethod(expense.getPaymentMethod())
                .paymentReference(expense.getPaymentReference())
                .paymentDate(expense.getPaymentDate())
                .status(expense.getStatus())
                .submittedBy(expense.getSubmittedBy())
                .submittedAt(expense.getSubmittedAt())
                .approvedBy(expense.getApprovedBy())
                .approvedAt(expense.getApprovedAt())
                .rejectionReason(expense.getRejectionReason())
                .isReimbursable(expense.getIsReimbursable())
                .reimbursed(expense.getReimbursed())
                .reimbursementDate(expense.getReimbursementDate())
                .odometerReading(expense.getOdometerReading())
                .location(expense.getLocation())
                .notes(expense.getNotes())
                .tags(expense.getTags())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .createdBy(expense.getCreatedBy())
                .updatedBy(expense.getUpdatedBy())
                .build();
    }
}
