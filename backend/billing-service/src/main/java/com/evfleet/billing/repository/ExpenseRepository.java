package com.evfleet.billing.repository;

import com.evfleet.billing.entity.Expense;
import com.evfleet.billing.entity.ExpenseCategory;
import com.evfleet.billing.entity.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Find by entity
    List<Expense> findByEntityTypeAndEntityId(String entityType, Long entityId);

    // Find by vehicle
    List<Expense> findByVehicleId(Long vehicleId);

    // Find by driver
    List<Expense> findByDriverId(Long driverId);

    // Find by category
    List<Expense> findByCategory(ExpenseCategory category);

    // Find by status
    List<Expense> findByStatus(ExpenseStatus status);

    // Find pending approvals
    @Query("SELECT e FROM Expense e WHERE e.status = 'PENDING_APPROVAL' ORDER BY e.submittedAt DESC")
    List<Expense> findPendingApprovals();

    // Find by date range
    List<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by entity and date range
    @Query("SELECT e FROM Expense e WHERE e.entityType = :entityType AND e.entityId = :entityId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate")
    List<Expense> findByEntityAndDateRange(
        @Param("entityType") String entityType,
        @Param("entityId") Long entityId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Calculate total by category and date range
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.category = :category " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate AND e.status = 'APPROVED'")
    BigDecimal calculateTotalByCategory(
        @Param("category") ExpenseCategory category,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Calculate total by entity
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.entityType = :entityType " +
           "AND e.entityId = :entityId AND e.status = 'APPROVED'")
    BigDecimal calculateTotalByEntity(
        @Param("entityType") String entityType,
        @Param("entityId") Long entityId
    );

    // Find reimbursable expenses
    @Query("SELECT e FROM Expense e WHERE e.isReimbursable = true AND e.reimbursed = false " +
           "AND e.status = 'APPROVED' ORDER BY e.expenseDate DESC")
    List<Expense> findUnreimbursedExpenses();

    // Find by submitted by
    List<Expense> findBySubmittedByOrderBySubmittedAtDesc(String submittedBy);
}
